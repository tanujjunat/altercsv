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
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class ProcessCsvUpdated {
	
	private static String LOCATION_OF_THE_FILE="file:///C:/Catalog/testing/files/guestRoomHighlights.txt";
	private static String OUTPUT_FILE_LOCATION="file:///C:/Catalog/testing/files/guestRoomHighlightsOutput.txt";
	private static String ROW_PARTITION = "\\+_\\+";

	public static void main(String[] args) throws CsvValidationException, IOException {
		readFile();
		System.out.println("Processing Complete");
	}

	private static void readFile() throws IOException, CsvValidationException {
		Path path = Paths.get(URI.create(LOCATION_OF_THE_FILE));
	    try (Reader reader = Files.newBufferedReader(path)) {
	    	CSVReader csvReader = new CSVReaderBuilder(reader)
					.withCSVParser(new CSVParserBuilder()
										.withSeparator('|')
										.withQuoteChar('~')
										.withIgnoreQuotations(false)
										.build())
					.build();
	    	readEachLine(csvReader);
	    };
		
	}

	private static void readEachLine(CSVReader csvReader) throws CsvValidationException, IOException {
		String[] line;
		Path path = Paths.get(URI.create(OUTPUT_FILE_LOCATION));
		ICSVWriter writer = new CSVWriterBuilder(new FileWriter(path.toString()))
									.withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
									.withSeparator('|')
									.withEscapeChar(CSVWriter.NO_ESCAPE_CHARACTER)
									.build();
		int index=0;
		int notMarshaIndex = 1;
		
		Path readerPath = Paths.get(URI.create(LOCATION_OF_THE_FILE));
		try (Reader reader = Files.newBufferedReader(readerPath)) {
			CSVReader csvReader1 = new CSVReaderBuilder(reader).withCSVParser(
					new CSVParserBuilder().withSeparator('|').withQuoteChar('~').withIgnoreQuotations(false).build())
					.build();

			csvReader1.readNext();
			String[] arr = csvReader1.readNext();
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].split(ROW_PARTITION, -1).length == 1) {
					index = i;
					break;
				} else {
					notMarshaIndex = i;
				}

			}
		};
		
		while ((line = csvReader.readNext()) != null) {
			int rowsCount = line.length;
			int columnCount = line[notMarshaIndex].split(ROW_PARTITION,-1).length;
			String[] output = new String[rowsCount];
			int rowIndex = 0;			
			for (int i = 0; i < columnCount; i++) {
				for (int j = 0; j < rowsCount; j++) {
					if (j==index) {
						output[rowIndex] = line[index].split(ROW_PARTITION,-1)[0];
						rowIndex++;
					} else {
						String [] columnArray = line[j].split(ROW_PARTITION,-1);
						if (columnArray[i] == "") {
							output[rowIndex] = "";
						} else {
							if (columnArray[i].contains("~")) {
								String data = columnArray[i].replace("~", "|");
								output[rowIndex] = '"' + data + '"';
							} else {
								output[rowIndex] = columnArray[i];
							}
							
						}					
						rowIndex++;
					}			
				}
				rowIndex=0;
				//System.out.println(Arrays.asList(output));
				writer.writeNext(output);
				writer.flush();
			}
        }	
	}
}
