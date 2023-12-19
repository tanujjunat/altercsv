package com.csv.altercsv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class ProcessCsv {
	
	private static String LOCATION_OF_THE_FILE="file:///C:/Catalog/testing/files/guestRoomHighlights.txt";
	private static String OUTPUT_FILE_LOCATION="file:///C:/Catalog/testing/files/guestRoomHighlightsOutput.txt";

	public static void main(String[] args) throws CsvValidationException, IOException {
		readFile();
	}

	private static void readFile() throws IOException, CsvValidationException {
		Path path = Paths.get(URI.create(LOCATION_OF_THE_FILE));
	    try (Reader reader = Files.newBufferedReader(path)) {
	    	CSVReader csvReader = new CSVReaderBuilder(reader)
					.withSkipLines(1)
					.withCSVParser(new CSVParserBuilder().withSeparator('|').withIgnoreQuotations(true).build())
					.build();
	    	readEachLine(csvReader);
	    };
		
	}

	private static void readEachLine(CSVReader csvReader) throws CsvValidationException, IOException {
		String[] line;
		Path path = Paths.get(URI.create(OUTPUT_FILE_LOCATION));
		CSVWriter writer = new CSVWriter(new FileWriter(path.toString()));
		int index = 0;
		while ((line = csvReader.readNext()) != null) {
			int rowsCount = line.length;
			int columnCount = line[index].split("\\+_\\+",-1).length;
			String[] output = new String[rowsCount];
			int rowIndex = 0;			
			for (int i = 0; i < columnCount; i++) {
				for (int j = 0; j < rowsCount; j++) {
					String [] columnArray = line[j].split("\\+_\\+",-1);
					if (columnArray.length == 0) {
						output[rowIndex] = "";
					} else {
						output[rowIndex] = columnArray[i];
					}					
					rowIndex++;
				}
				rowIndex=0;
				System.out.println(Arrays.asList(output));
			}
			index++;
        }	
	}
}
