package com.csv.altercsv;

public class TestFormat {

	public static void main(String[] args) {
		String st = "hi there %s";
		String value = "tanuj";
		System.out.println(args);
		System.out.println(String.format(st, value));

	}

}
