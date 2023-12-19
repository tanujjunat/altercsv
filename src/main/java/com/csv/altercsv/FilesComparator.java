package com.csv.altercsv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FilesComparator {
	
	private static String INPUT1 ="file:///C:/Catalog/testing/files/guestRoomHighlightsSource.txt";
	private static String INPUT2 ="file:///C:/Catalog/testing/files/guestRoomHighlightsOutput.txt";
	private static String OUTPUT ="file:///C:/Catalog/testing/files/output.xlsx";
	private static final List<String> listOfRowsWithAdditionalColumnInSource = new ArrayList<>();
	private static final List<String> listOfRowsWithAdditionalColumnInStiboExport = new ArrayList<>();
	private static final String lineSeparator = "<br />";
	
	public static void main(String[] args) {
		compareFiles();
	}

	private static void compareFiles() {
		Path path1 = Paths.get(URI.create(INPUT1));
		Path path2 = Paths.get(URI.create(INPUT2));
		Path path3 = Paths.get(URI.create(OUTPUT));
		List<String> uniKeys = Arrays.asList("a_marsha_code","eghigh_a_unique_id");
		
		List<Map<String, String>> inputFileValuesAll;
		try {
			long startTime = System.currentTimeMillis();
			inputFileValuesAll = readSourceFileInTextFormat(path1.toString(), "\\|");
			List<Map<String, String>> outputFileValuesAll = readExportFileInTextFormat(path2.toString(), "\\|");
			
	        //Input and Output files values/rows after removal of rows with additional column data
	        List<Map<String,String>> inputFileValues = removingRowsWithAdditionalColumnsFromSourceWithTwoKeys(inputFileValuesAll,uniKeys);
	        List<Map<String,String>> outputFileValues = removingRowsWithAdditionalColumnsFromExportWithTwoKeys(outputFileValuesAll,uniKeys);
			getMismatchingRowsAndCountWithTwoKeys(uniKeys, inputFileValuesAll, outputFileValuesAll);
			findMatchingRowsWithTwoKeysAndCompareData(uniKeys, inputFileValuesAll, outputFileValuesAll, path3);
			System.out.println("Processing Complete in "+ (System.currentTimeMillis() - startTime) + " sec");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<String> compareTwoList(List<String> list1, List<String> list2) {
        List<String> temp = new ArrayList<>(list2);
        temp.removeAll(list1);
        return temp;
    }
	
	private static void getMismatchingRowsAndCountWithTwoKeys(List<String> uniKeys, List<Map<String, String>> inputFileValues, List<Map<String, String>> outputFileValues) {
        List<String> missingOrAdditionalInSource;
        List<String> missingOrAdditionalInExport;
        List<String> inputUniKeyValues = new ArrayList<>();
        List<String> outputUniKeyValues = new ArrayList<>();
        for (Map<String, String> eachInput : inputFileValues) {
            inputUniKeyValues.add(eachInput.get(uniKeys.get(0)) + "-" + eachInput.get(uniKeys.get(1)));
        }
        for (Map<String, String> eachOutput : outputFileValues) {
            outputUniKeyValues.add(eachOutput.get(uniKeys.get(0)) + "-" + eachOutput.get(uniKeys.get(1)));
        }
        Collections.sort(inputUniKeyValues);
        Collections.sort(outputUniKeyValues);
        missingOrAdditionalInSource = compareTwoList(outputUniKeyValues, inputUniKeyValues);
        missingOrAdditionalInExport = compareTwoList(inputUniKeyValues, outputUniKeyValues);
//        Set<String> unmatchedSet = new HashSet<>(missingOrAdditionalInExport);

        if (!missingOrAdditionalInSource.isEmpty()) {
        	System.out.println("Data for the following unique key are additional/missing in the " +
                    "Source File." + lineSeparator + "Number of additional/missing rows/records : "
                    + missingOrAdditionalInSource.size() + lineSeparator + missingOrAdditionalInSource);
        } else if (!missingOrAdditionalInExport.isEmpty()) {
        	System.out.println("Data for the following unique key are additional in the " +
                    "output file. " + lineSeparator + "Number of additional rows/records : "
                    + missingOrAdditionalInExport.size() + lineSeparator + missingOrAdditionalInExport);
        } else {
        	System.out.println("Both input and output files have equal and matching unique key values.");
        }
    }
	
	private static void findMatchingRowsWithTwoKeysAndCompareData(List<String> uniKeys, List<Map<String, String>> inputFileValues, List<Map<String, String>> outputFileValues, Path path3) {
        Map<String, String> inputRowToMatch;
        List<String> failedObjectsToWriteInExcel = new ArrayList<>();
        List<String> failedAttributeKeysToWriteInExcel = new ArrayList<>();
        List<String> failedMessagesToWriteInExcel = new ArrayList<>();
        Map<String, String> outputRowToMatch = new LinkedHashMap<>();
        int i = 1;

        for (Map<String, String> eachMapInput : inputFileValues) {
            String firstUniqueValueInSourceToFind = eachMapInput.get(uniKeys.get(0));
            String secondUniqueValueInSourceToFind = eachMapInput.get(uniKeys.get(1));
            inputRowToMatch = eachMapInput;
            String firstUniqueValueInExportToMatch, secondUniqueValueInExportToMatch;
            Iterator<Map<String, String>> outputIterator = outputFileValues.iterator();
            while (outputIterator.hasNext()) {
                Map<String, String> eachMapOutput = outputIterator.next();
                firstUniqueValueInExportToMatch = eachMapOutput.get(uniKeys.get(0));
                secondUniqueValueInExportToMatch = eachMapOutput.get(uniKeys.get(1));
                if (firstUniqueValueInSourceToFind.equalsIgnoreCase(firstUniqueValueInExportToMatch)) {
                    if (secondUniqueValueInSourceToFind.equalsIgnoreCase(secondUniqueValueInExportToMatch)) {
                        outputRowToMatch = eachMapOutput;
                        outputIterator.remove();
                        break;
                    } else {
                        outputRowToMatch = new LinkedHashMap<>();
                    }
                } else {
                    outputRowToMatch = new LinkedHashMap<>();
                }
            }
            boolean flag;
            Map<String, Boolean> resultMap;
            if (outputRowToMatch.size() != 0) {
//                if(inputDataColCount1==outputDataColCount1) {
                resultMap = areEqualKeyValuesBetweenMaps(inputRowToMatch, outputRowToMatch);
                List<Map.Entry<String, Boolean>> mismatchedOnesInResult = new ArrayList<>();
                for (Map.Entry<String, Boolean> e : resultMap.entrySet()) {
                    flag = e.getValue();
                    if (!flag) {
                        mismatchedOnesInResult.add(e);
                    }
                }
                if (mismatchedOnesInResult.isEmpty()) {
                    //System.out.println("Fields are matching");
                } else {
                	System.out.println("Mismatch observed! for: " + firstUniqueValueInSourceToFind
                            + "-" + secondUniqueValueInSourceToFind);
                    for (Map.Entry<String, Boolean> each : mismatchedOnesInResult) {
                        String failedAttribute = each.getKey();
                        failedObjectsToWriteInExcel.add(firstUniqueValueInSourceToFind + "-" + secondUniqueValueInSourceToFind);
                        failedAttributeKeysToWriteInExcel.add(failedAttribute);
                        System.out.println("mismatching Input file data: " + failedAttribute +
                                ": " + inputRowToMatch.get(failedAttribute) + "  and Output file data: " +
                                failedAttribute + ": " + outputRowToMatch.get(failedAttribute) + lineSeparator);
                        String failureDetail = "mismatching Input file data: " + failedAttribute + ": " +
                                inputRowToMatch.get(failedAttribute) + "  and Output file data: " + failedAttribute + ": " + outputRowToMatch.get(failedAttribute);
                        failedMessagesToWriteInExcel.add(failureDetail);
                    }
                }
                outputRowToMatch = new LinkedHashMap<>();
            } else {
            	System.out.println("No related data found for " + firstUniqueValueInSourceToFind + "-" + secondUniqueValueInSourceToFind + " to compare in output file.");
            }
            i++;
        }
        writeFailedValuesToExcel(failedObjectsToWriteInExcel, failedAttributeKeysToWriteInExcel, failedMessagesToWriteInExcel, path3);
    }
	
	private static Map<String, Boolean> areEqualKeyValuesBetweenMaps(Map<String, String> first, Map<String, String> second) {

        return first.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().equalsIgnoreCase(second.get(e.getKey()))));

    }
	
	
	
	public static List<Map<String, String>> readSourceFileInTextFormat(String filePath, String delimiter) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        ArrayList<String> headerValues = getHeaderValues(br);
        List<Map<String, String>> listOfMapsInSource = getRowValuesInSource(delimiter, br, headerValues);
        
        return listOfMapsInSource;
    }
	
	public static List<Map<String, String>> readExportFileInTextFormat(String filePath, String delimiter) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        ArrayList<String> headerValues = getHeaderValues(br);
        int totalHeaders = headerValues.size();
        List<Map<String, String>> listOfMapsInStiboExport = getRowValuesInExport(delimiter, br, headerValues, totalHeaders);
        return listOfMapsInStiboExport;
    }
	
	private static ArrayList<String> getHeaderValues(BufferedReader br) throws IOException {
        ArrayList<String> headerValues = new ArrayList<>();
        String firstLine = br.readLine();
        if (firstLine != null) {
            String[] temp = firstLine.split("\\|");
            Collections.addAll(headerValues,temp);
        }
        return headerValues;
    }
	
	private static List<Map<String, String>> getRowValuesInSource(String delimiter, BufferedReader br, ArrayList<String> headerValues) throws IOException {
        Map<String, String> eachRowValuesAsMap = new LinkedHashMap<>();
        List<Map<String, String>> listOfMapsInSource = new ArrayList<>();
        String restOfTheLines = br.readLine();
        while ((restOfTheLines != null)) {
            String temp = restOfTheLines;
            String[] eachRowValuesAfterSplit = restOfTheLines.split(delimiter);
            if(eachRowValuesAfterSplit.length== headerValues.size() || eachRowValuesAfterSplit.length< headerValues.size()) {
                for (int i = 0; i < eachRowValuesAfterSplit.length; i++) {
                    eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].replaceAll("^\\s*|\\s*$", "");
                    if (eachRowValuesAfterSplit[i].endsWith(";")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(0, eachRowValuesAfterSplit[i].lastIndexOf(";"));
                    } else if (eachRowValuesAfterSplit[i].startsWith("'") && eachRowValuesAfterSplit[i].endsWith("'")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(1, eachRowValuesAfterSplit[i].lastIndexOf("'"));
                    } else if (eachRowValuesAfterSplit[i].startsWith("\"") && eachRowValuesAfterSplit[i].endsWith("\"")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(1);
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(0, eachRowValuesAfterSplit[i].length() - 1);
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].replaceAll("\\s*$", "");
                    } else if (eachRowValuesAfterSplit[i].startsWith("snlQt#\"") && eachRowValuesAfterSplit[i].endsWith("\"")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(7);
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(0, eachRowValuesAfterSplit[i].length() - 1);
                    } else if (eachRowValuesAfterSplit[i].startsWith("'snlQt#") && eachRowValuesAfterSplit[i].endsWith("'")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(7, eachRowValuesAfterSplit[i].lastIndexOf("'"));
                    } else if (eachRowValuesAfterSplit[i].startsWith("snlQt#\"")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(7);
                    } else if(eachRowValuesAfterSplit[i].startsWith("snlQt#")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(6);
                    } else if (eachRowValuesAfterSplit[i].equalsIgnoreCase("[delete]")) { //Per DMP-6027 story's Dev ask, adding below changes on 12-Dec'22 for Delete handling
                        eachRowValuesAfterSplit[i] = "";
                    } else if (eachRowValuesAfterSplit[i].contains("[delete];")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].replace("[delete];", "");
                    } else if (eachRowValuesAfterSplit[i].endsWith("$")) {
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].substring(0, eachRowValuesAfterSplit[i].length() - 1);
                        eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].replaceAll("\\s$", "");
                    }
                    eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].replaceAll("^\\s*", "");
                    eachRowValuesAfterSplit[i] = eachRowValuesAfterSplit[i].replaceAll("\\s*$", "");
                    eachRowValuesAsMap.put(headerValues.get(i), eachRowValuesAfterSplit[i]);
                }
                listOfMapsInSource.add(eachRowValuesAsMap);
                eachRowValuesAsMap = new LinkedHashMap<>();
            } else {
                listOfRowsWithAdditionalColumnInSource.add(temp);
            }
            restOfTheLines = br.readLine();
        }
        return listOfMapsInSource;
    }
	
	public static List<Map<String,String>> removingRowsWithAdditionalColumnsFromExportWithTwoKeys(
            List<Map<String,String>> exportFileMaps,List<String> uniqueKeys) {
        List<Map<String,String>> finalizedExportFileMaps = new ArrayList<>(exportFileMaps);
        for (String eachValue: listOfRowsWithAdditionalColumnInSource) {
            for (Map<String, String> eachMap:exportFileMaps) {
                if(eachValue.contains(eachMap.get(uniqueKeys.get(0))) && eachValue.contains(eachMap.get(uniqueKeys.get(1)))) {
                    finalizedExportFileMaps.remove(eachMap);
                }
            }
        }
        return finalizedExportFileMaps;
    }
	
	public static List<Map<String,String>> removingRowsWithAdditionalColumnsFromSourceWithTwoKeys(
            List<Map<String,String>> sourceFileMaps,List<String> uniqueKeys) {
        List<Map<String,String>> finalizedSourceFileMaps = new ArrayList<>(sourceFileMaps);
        for (String eachValue: listOfRowsWithAdditionalColumnInStiboExport) {
            for (Map<String, String> eachMap:sourceFileMaps) {
                if(eachValue.contains(eachMap.get(uniqueKeys.get(0))) && eachValue.contains(eachMap.get(uniqueKeys.get(1)))) {
                    finalizedSourceFileMaps.remove(eachMap);
                }
            }
        }
        return finalizedSourceFileMaps;
    }
	
	private static List<Map<String, String>> getRowValuesInExport(String delimiter, BufferedReader br, ArrayList<String> headerValues, int totalHeaders) throws IOException {
        Map<String, String> eachRowValuesInExportAsMap = new LinkedHashMap<>();
        List<Map<String, String>> listOfMapsInStiboExport = new ArrayList<>();
        String restOfTheLines = br.readLine();
        while ((restOfTheLines != null)) {
            String tempRows = restOfTheLines;
            String[] eachRowValuesInExportAfterSplit = restOfTheLines.split(delimiter);
            if(eachRowValuesInExportAfterSplit.length== headerValues.size() || eachRowValuesInExportAfterSplit.length< headerValues.size()) {
                for (int i = 0; i < eachRowValuesInExportAfterSplit.length; i++) {
                    if (eachRowValuesInExportAfterSplit[i].endsWith(";")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].substring(0, eachRowValuesInExportAfterSplit[i].lastIndexOf(";"));
                    } else if (eachRowValuesInExportAfterSplit[i].startsWith("'snlQt#") && eachRowValuesInExportAfterSplit[i].endsWith("'")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].substring(7, eachRowValuesInExportAfterSplit[i].lastIndexOf("'"));
                    } else if (eachRowValuesInExportAfterSplit[i].startsWith("snlQt#\"") && eachRowValuesInExportAfterSplit[i].endsWith("\"")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].substring(7);
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].replaceAll("\"$", "");
                    } else if (eachRowValuesInExportAfterSplit[i].startsWith("\"") && eachRowValuesInExportAfterSplit[i].endsWith("\"")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].substring(1);
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].replaceAll("\"$", "");
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].replaceAll("\\s*$", "");
                    } else if (eachRowValuesInExportAfterSplit[i].startsWith("snlQt#")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].substring(6);
                    } else if (eachRowValuesInExportAfterSplit[i].endsWith("$")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].substring(0, eachRowValuesInExportAfterSplit[i].length() - 1);
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].replaceAll("\\s$", "");
                    } else if (eachRowValuesInExportAfterSplit[i].startsWith("\"")) {
                        eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].replaceAll("\"", "");
                    }
                    eachRowValuesInExportAfterSplit[i] = eachRowValuesInExportAfterSplit[i].replaceAll("^\\s*|\\s*$", "");
                    eachRowValuesInExportAsMap.put(headerValues.get(i), eachRowValuesInExportAfterSplit[i]);
                }
                int sizeofMap = eachRowValuesInExportAsMap.size();
                if (sizeofMap != totalHeaders) {
                    for (int k = sizeofMap; k < totalHeaders; k++) {
                        eachRowValuesInExportAsMap.put(headerValues.get(k), "");
                    }
                }
                listOfMapsInStiboExport.add(eachRowValuesInExportAsMap);
                eachRowValuesInExportAsMap = new LinkedHashMap<>();
            } else {
                listOfRowsWithAdditionalColumnInStiboExport.add(tempRows);
            }
            restOfTheLines = br.readLine();
        }
        return listOfMapsInStiboExport;
    }
	
	public static void writeFailedValuesToExcel(List<String> failedObjects, List<String> keys, List<String> message, Path path3) {
        int i = 1, k = 0;
        try {
            FileInputStream fis = new FileInputStream(path3.toString());
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sh = wb.getSheetAt(0);
            wb.removePrintArea(0);

            String[] columnHeading = {"Failed Object", "Attribute key", "Failure message"};
            Row headerRow = sh.createRow(0);
            for (int j = 0; j < columnHeading.length; j++) {
                headerRow.createCell(j);
                sh.getRow(0).createCell(j).setCellValue(columnHeading[j]);
                Font headerFont = wb.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.YELLOW.index);
            }
            while (k < failedObjects.size()) {
                Row row = sh.createRow(i);
                row.createCell(0);
                sh.getRow(i).createCell(0).setCellValue(failedObjects.get(k));
                sh.getRow(i).createCell(1).setCellValue(keys.get(k));
                sh.getRow(i).createCell(2).setCellValue(message.get(k));
                k++;
                i++;
            }
            fis.close();
            FileOutputStream fos = new FileOutputStream(path3.toString());
            wb.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
