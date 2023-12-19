package com.csv.altercsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.diff.MultiLevelByNameAndTextSelector;

public class NewXMLComparator {

	public static void main(String[] args) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {

		File expectedFile = new File("C:/Catalog/testing/files/xmlCompare/masterFeedSource.xml");
		File actualFile = new File("C:/Catalog/testing/files/xmlCompare/masterFeedTarget.xml");
		//File pathsToIgnoreFile = new File("C:/Catalog/testing/files/xmlCompare/PathsToIgnore.txt");
		//File pathsToIgnoreFile = new File("C:/Catalog/testing/obsolete.txt");
		//Path xpathsToIgnoreFilePath = Paths.get(URI.create("file:///C:/Catalog/testing/files/xmlCompare/PathsToIgnore.txt"));
		Path xpathsToIgnoreFilePath = Paths.get(URI.create("file:///C:/Catalog/testing/obsolete.txt"));
		Path differenceFilePath = Paths.get(URI.create("file:///C:/Catalog/testing/files/xmlCompare/MasterFeedDifference.xlsx"));
		
		
		//printPathsToIgnore(expectedFile, xpathsToIgnoreFilePath);
		Iterable<Difference> differences = compareXML(expectedFile, actualFile);
		printDifferences(differences, expectedFile, actualFile);
		//writeDifferencesInExcel(differences, differenceFilePath, expectedFile, actualFile);
		System.out.println("Completed");
	}

	public static void printPathsToIgnore(File targetFile, Path pathsToIgnoreFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		List<String> xPathList = Files.readAllLines(pathsToIgnoreFile);
		int i=0;
		for (String xPath : xPathList) {
			i++;
			String evaluatedValue = evaluateXpath(targetFile, xPath);
			System.out.println(evaluatedValue);
			System.out.println(i);
			System.out.println("####################################################################################################################");
		}
		
		
	}

	public static Iterable<Difference> compareXML(File source, File target) throws SAXException, IOException {
		ElementSelector elementSelector = ElementSelectors.conditionalBuilder()
				.whenElementIsNamed("GuestRoomInfo").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("LocationCategory").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("HotelCategory").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("SegmentCategory").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("Renovation").thenUse(ElementSelectors.byNameAndAttributes("AreaText"))
				.whenElementIsNamed("Description").thenUse(ElementSelectors.byNameAndAttributes("Name"))
				.whenElementIsNamed("ListItem").thenUse(ElementSelectors.byNameAndAttributes("ListItem"))
				.whenElementIsNamed("Service").thenUse(ElementSelectors
						.and(ElementSelectors.byNameAndAttributes("Code","BusinessServiceCode"), 
							 ElementSelectors.byXPath("RelativePosition", ElementSelectors.byNameAndAttributes("Distance"))
						))
				
				//.whenElementIsNamed("Service").thenUse(ElementSelectors.byNameAndAttributes("Code","BusinessServiceCode","Sort"))
				.whenElementIsNamed("Feature").thenUse(ElementSelectors.byNameAndAttributes("AccessibleCode","SecurityCode"))
				.whenElementIsNamed("ArchitecturalStyle").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("Amenity").thenUse(ElementSelectors.byNameAndAttributes("RoomAmenityCode"))
				.whenElementIsNamed("OwnershipManagementInfo").thenUse(ElementSelectors.byNameAndAttributes("RelationshipTypeCode"))
				.whenElementIsNamed("Code").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("MeetingRoomCapacity").thenUse(ElementSelectors.byNameAndAttributes("MeetingRoomFormatCode"))
				.whenElementIsNamed("Restaurant").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("OperationTime").thenUse(ElementSelectors.byNameAndAttributes("Start", "End"))
				.whenElementIsNamed("RestaurantDescription").thenUse(ElementSelectors.byNameAndAttributes("Name"))
				.whenElementIsNamed("MeetingRoom").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("PolicyInfoCode").thenUse(ElementSelectors.byNameAndAttributes("Name"))
				.whenElementIsNamed("TaxPolicy").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("GuaranteePayment").thenUse(ElementSelectors.byNameAndAttributes("PaymentCode", "Type"))
				.whenElementIsNamed("CheckoutCharge").thenUse(ElementSelectors.byNameAndAttributes("CodeDetail"))
				.whenElementIsNamed("Attraction").thenUse(ElementSelectors.byNameAndAttributes("AttractionCategoryCode", "ID"))
				.whenElementIsNamed("Transportation").thenUse(ElementSelectors.byNameAndAttributes("TransportationCode"))
				.whenElementIsNamed("Recreation").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("RecreationDetail").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("Award").thenUse(ElementSelectors.byNameAndAttributes("Provider"))
				.whenElementIsNamed("Phone").thenUse(ElementSelectors.byNameAndAttributes("PhoneTechType","PhoneLocationType","PhoneUseType"))
				.whenElementIsNamed("JobTitle").thenUse(ElementSelectors.byNameAndAttributes("Type"))
				.whenElementIsNamed("TPA_Ext_CreditCard").thenUse(ElementSelectors.byNameAndAttributes("CreditCardName"))
				.whenElementIsNamed("TPA_Ext_RegionInfos_RegionInfo").thenUse(ElementSelectors.byNameAndAttributes("Region", "SubRegion"))
				.whenElementIsNamed("TPA_Ext_PropertyInfo").thenUse(ElementSelectors.byNameAndAttributes("Name"))
				.whenElementIsNamed("TPA_Ext_MessageType").thenUse(ElementSelectors.byNameAndAttributes("RecordID"))
				.whenElementIsNamed("TPA_Ext_eConfo").thenUse(ElementSelectors.byNameAndAttributes("RecordID"))
				.whenElementIsNamed("TPA_Ext_Seasonality").thenUse(ElementSelectors.byNameAndAttributes("RecordID"))
				.whenElementIsNamed("TPA_Ext_ConnectivityType").thenUse(ElementSelectors.byNameAndAttributes("DataConnectionType"))
				.whenElementIsNamed("TPA_Ext_GolfClub").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("TPA_Ext_GolfCourse").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("TPA_Ext_Restaurant").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("TPA_Green_Program").thenUse(ElementSelectors.byNameAndAttributes("Code","Name"))
				.whenElementIsNamed("TPA_Green_Measure").thenUse(ElementSelectors.byNameAndAttributes("Name"))
				.whenElementIsNamed("TPA_Ext_Oversold").thenUse(ElementSelectors.byNameAndAttributes("NonReferralsHotelName"))
				.whenElementIsNamed("TPA_Ext_TierType").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("TPA_Ext_Seasonal_Award").thenUse(ElementSelectors.byNameAndAttributes("RecordID"))
				.whenElementIsNamed("TPA_Ext_Featured_Amenity").thenUse(ElementSelectors.byNameAndAttributes("ID"))
				.whenElementIsNamed("TPA_Ext_AlternatePayment").thenUse(ElementSelectors.byNameAndAttributes("AlternatePaymentName"))
				.whenElementIsNamed("TPA_Ext_AccessibleFeature").thenUse(ElementSelectors.byNameAndAttributes("Code"))
				.whenElementIsNamed("TPA_Ext_Translated_Language").thenUse(ElementSelectors.byNameAndAttributes("LocaleCode"))
				.whenElementIsNamed("TPA_Ext_ConnectivityService").thenUse(ElementSelectors.byNameAndAttributes("ConnectivityServiceType"))
				.whenElementIsNamed("TPA_Ext_ConnectivityType").thenUse(ElementSelectors.byNameAndAttributes("DataConnectionType"))
				.whenElementIsNamed("TPA_Ext_InLang_MessageType").thenUse(ElementSelectors.byNameAndAttributes("MessageType"))
				.whenElementIsNamed("TPA_Property_Address").thenUse(ElementSelectors.byNameAndAttributes("AddressType"))
				.whenElementIsNamed("LoyalProgram").thenUse(ElementSelectors.byNameAndAttributes("ProgramName"))
				.whenElementIsNamed("PropertyPersonnel").thenUse(ElementSelectors.byNameAndAttributes("Type"))
				.whenElementIsNamed("OperationSchedule").thenUse(ElementSelectors.byXPath("Charge", ElementSelectors.byNameAndAttributes("Code", "ChargeUnit")))
				.whenElementIsNamed("ContactInfo").thenUse(ElementSelectors.byNameAndAttributes("ContactProfileType"))
				.whenElementIsNamed("PenaltyDescription").thenUse(ElementSelectors.byNameAndAttributes("Name"))
				.whenElementIsNamed("Address").thenUse(ElementSelectors.byNameAndAttributes("UseType"))
				.whenElementIsNamed("GDS_Code").thenUse(ElementSelectors.byNameAndAttributes("GDS_PropertyCode"))
				.elseUse(ElementSelectors.byName)
				.build();
		Path filePath = Paths.get(URI.create("file:///C:/Catalog/testing/files/xmlCompare/MasterFeedSimilar.txt"));
		if (Files.exists(filePath)) {
    		Files.delete(filePath);
    		Files.createFile(filePath);
    	} else {
    		Files.createFile(filePath);
    	}

		Diff xmlDiff = DiffBuilder
							.compare(Input.fromFile(source))
							.withTest(Input.fromFile(target))
							.checkForIdentical()
							.ignoreComments()
							.ignoreWhitespace()
							.ignoreElementContentWhitespace()
							.withNodeMatcher(new DefaultNodeMatcher(elementSelector))
							.withDifferenceEvaluator(((comparison, outcome) -> {
								if (outcome == ComparisonResult.DIFFERENT
										&& (comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE
												|| comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
												|| comparison.getType() == ComparisonType.ELEMENT_NUM_ATTRIBUTES
												|| comparison.toString().contains("@LastModifyDateTime")
												||  comparison.toString().contains("@LastUpdated"))) {
									return ComparisonResult.EQUAL;
								} /*else if ((outcome == ComparisonResult.EQUAL || outcome == ComparisonResult.SIMILAR)
										&& (comparison.getType() == ComparisonType.ATTR_VALUE
												|| comparison.getType() == ComparisonType.TEXT_VALUE)) {
									try {
										Files.writeString(filePath, comparison.toString(), StandardOpenOption.APPEND);
										Files.writeString(filePath, "\nxPath : " + comparison.getControlDetails().getXPath() + "\n", StandardOpenOption.APPEND);
										
										Files.writeString(filePath, "\nNode : " + getNodeValue(source, comparison.getControlDetails().getXPath()) + "\n", StandardOpenOption.APPEND);
										Files.writeString(filePath, "\n####################################################################################################\n", StandardOpenOption.APPEND);
									} catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException | TransformerFactoryConfigurationError | TransformerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								}*/
								
								/*else if ((outcome == ComparisonResult.EQUAL || outcome == ComparisonResult.SIMILAR)
										&& !(comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE
												|| comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH
												|| comparison.getType() == ComparisonType.ELEMENT_NUM_ATTRIBUTES
												|| comparison.getType() == ComparisonType.NODE_TYPE
												|| comparison.getType() == ComparisonType.NAMESPACE_PREFIX
												|| comparison.getType() == ComparisonType.NAMESPACE_URI
												|| comparison.getType() == ComparisonType.ATTR_NAME_LOOKUP)) {
									
									try {
										Files.writeString(filePath, comparison.toString(), StandardOpenOption.APPEND);
										Files.writeString(filePath, "\n####################################################################################################\n", StandardOpenOption.APPEND);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}*/
								return outcome;
							}))				
							.build();
		   
        return xmlDiff.getDifferences();
	}

	public static void printDifferences(Iterable<Difference> differences, File source, File target) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		int count = 0;
		for (Difference difference : differences) {
			count++;		
			System.out.println(difference);
			
			if (difference.getComparison().getControlDetails().getXPath() != null && difference.getComparison().getTestDetails().getXPath() == null) {
				System.out.println("SIH Servlet : " + evaluateXpath(source, difference.getComparison().getControlDetails().getXPath()));
				System.out.println("Ota Master Feed : Not Present");
			} 
			if (difference.getComparison().getControlDetails().getXPath() == null && difference.getComparison().getTestDetails().getXPath() != null) {
				System.out.println("SIH Servlet : Not Present");
				System.out.println("Ota Master Feed : " + evaluateXpath(target, difference.getComparison().getTestDetails().getXPath()));
			}
			/*if (ComparisonType.ATTR_NAME_LOOKUP.equals(difference.getComparison().getType())) {
				System.out.println("SIH Servlet : " + evaluateXpath(source, difference.getComparison().getControlDetails().getXPath()));
				System.out.println("Ota Master Feed : " + evaluateXpath(target, difference.getComparison().getTestDetails().getXPath()));
			}*/
			System.out.println("#############################################################################################################################");
		}
		System.out.println("###############################");
		System.out.println("Total Differences : " + count);
		System.out.println("###############################");
	}
	
	public static String evaluateXpath(File xmlFile, String xPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		Document xmlDocument = getDocumentFromFile(xmlFile);
        XPath xpath = getXPath();
        String nodeString = null;
        
        Node value = (Node)xpath.evaluate(xPath, xmlDocument, XPathConstants.NODE);
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(value), new StreamResult(writer));
        nodeString = writer.toString();
        if (StringUtils.isEmpty(nodeString)) {
        	nodeString = xpath.evaluate(xPath, xmlDocument);
        }
                
        return nodeString;
	}
	
	public static String getNodeValue(File xmlFile, String xPath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		Document xmlDocument = getDocumentFromFile(xmlFile);
        XPath xpath = getXPath();
        
        Node value = (Node)xpath.evaluate(xPath, xmlDocument, XPathConstants.NODE);
        
                
        return value.getNodeName()+":"+ value.getNodeValue();
	}

	private static XPath getXPath() {
		XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
		return xpath;
	}

	private static Document getDocumentFromFile(File xmlFile)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document xmlDocument = db.parse(xmlFile);
        xmlDocument.getDocumentElement().normalize();
		return xmlDocument;
	}
	
	public static void writeDifferencesInExcel(Iterable<Difference> differences, Path excelPath, File source, File target) throws XPathExpressionException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
        try {
        	int i=1;
        	FileInputStream fis = null;
        	XSSFWorkbook wb = null;
        	try {
        		fis = new FileInputStream(excelPath.toString());
        		wb = new XSSFWorkbook(fis);
        	} catch (Exception e) {
        		wb = new XSSFWorkbook();
        		wb.createSheet();
        	}
             
            wb.setSheetName(0, "Differences");
            XSSFSheet sh = wb.getSheetAt(0);
            wb.removePrintArea(0);

            String[] columnHeading = {"Difference", "SIH Hotel XML", "Ota Master Feed"};
            Row headerRow = sh.createRow(0);
            for (int j = 0; j < columnHeading.length; j++) {
                headerRow.createCell(j);
                sh.getRow(0).createCell(j).setCellValue(columnHeading[j]);
                Font headerFont = wb.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.YELLOW.index);
            }
            Iterator<Difference> differencesInterator = differences.iterator();
            while (differencesInterator.hasNext()) {
            	Difference difference = differencesInterator.next();
                Row row = sh.createRow(i);
                row.createCell(0);
                sh.getRow(i).createCell(0).setCellValue(difference.toString());
                if (difference.getComparison().getControlDetails().getXPath() != null && difference.getComparison().getTestDetails().getXPath() == null) {
                	sh.getRow(i).createCell(1).setCellValue(evaluateXpath(source, difference.getComparison().getControlDetails().getXPath()));
                }
                if (difference.getComparison().getControlDetails().getXPath() == null && difference.getComparison().getTestDetails().getXPath() != null) {
                	sh.getRow(i).createCell(2).setCellValue(evaluateXpath(target, difference.getComparison().getTestDetails().getXPath()));
                }               
                i++;
            }                
                
            
            FileOutputStream fos = new FileOutputStream(excelPath.toString());
            wb.write(fos);
            fos.close();
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
