package com.csv.altercsv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

public class GmtFormat {

	public static void main(String[] args) {
		
		/*
		 * String split = "; |;"; String s1 = "tanuj;Arnold"; String s2 =
		 * "tanuj; Arnold"; System.out.println(s1.split(split)[0]);
		 * System.out.println(s1.split(split)[1]);
		 * System.out.println(s2.split(split)[0].length());
		 * System.out.println(s2.split(split)[1].length());
		 */
		
		roundOff("123.4");
		
		System.out.println(getGmtOffsetFormat("45"));
		//System.out.println(formatDate(null));
		String brandId = "CC";
		String subBrandId = "JWM";
		System.out.println(getPropertyBrandId(brandId, subBrandId));
		System.out.println(isNewlyBuilt("2022-09-25"));
	}
	
	

	private static void roundOff(String st) {
		String numberAfterRoundOff = null;
		if (StringUtils.isNoneBlank(st)) {
			Double numberBeforeRoundOff = Double.parseDouble(st);
			numberAfterRoundOff = String.valueOf(Math.round(numberBeforeRoundOff));
		}
		System.out.println(numberAfterRoundOff);	
	}



	private static boolean isNewlyBuilt(String openingDateString) {
		boolean isNewlyBuilt = Boolean.FALSE;
		if (StringUtils.isNotBlank(openingDateString)) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate openingDate = LocalDate.parse(openingDateString, formatter);
			LocalDate currentDate = LocalDate.now();
		    LocalDate currentDateMinus1Year = currentDate.minusYears(1);
		    if (!openingDate.isBefore(currentDateMinus1Year)) {
		    	isNewlyBuilt = Boolean.TRUE;
		    }
		}			
		return isNewlyBuilt;
	}



	private static String getPropertyBrandId(String brandId, String subBrandId) {
		if ("MC".equalsIgnoreCase(brandId)) {
			if ("JWM".equalsIgnoreCase(subBrandId)) {
				return "JW";
			}
		} else if ("VB".equalsIgnoreCase(brandId)) {
			return subBrandId;
		} else if ("ET".equalsIgnoreCase(brandId) || "CC".equalsIgnoreCase(brandId)) {
			return "MC";
		}
		return brandId;
	}



	private static String getGmtOffsetFormat(String timeDiff) {
		int timeDiffInInt = Integer.parseInt(timeDiff);
		int hours = (int) (timeDiffInInt/2.0 - 5);
		double floatDiff = timeDiffInInt/2.0 - 5;
		int time = (int) ((floatDiff) * 60 % 60);
		if (hours < 0 && time > 0) {
			time = - time;
		}
		ZoneOffset gmtOffset = ZoneOffset.ofHoursMinutes(hours, time);
		return gmtOffset.toString() == "Z" ? "+00:00" : gmtOffset.toString();		
	}
	
	private static String formatDate(String inputDateString) {
		String formattedDate = null;
		if (inputDateString == null) {
			formattedDate = "Kdate";
		} else {
			try {
				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat outputFormat = new SimpleDateFormat("ddMMMyy");
				Date inputDate = inputFormat.parse(inputDateString);
				formattedDate = outputFormat.format(inputDate);
			} catch (ParseException e) {
				//log here
			}
		}		
		return formattedDate;
	}

}
