package com.csv.altercsv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CSVReader1 {

	public static void main(String[] args) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader("C:/Softwares/Hotel.txt"));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            System.out.println(row[0] + "," + row[1] + "," + row[2] + "," + row[3]);
        }
    }
}
