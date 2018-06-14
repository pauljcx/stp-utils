/* MIT License
 *
 * Copyright (c) 2018 Paul Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.stp.util;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/* @author Paul Collins
 * @version v1.0 ~ 03/10/2018
 * HISTORY: Version 1.0 created utility class for parsing date values from text by looking for common format patterns
 */
public class CustomDateFormat extends SimpleDateFormat {
	private static final Logger logger = Logger.getLogger(CustomDateFormat.class.getName());
	public static final long serialVersionUID = 1L;
	
	public static final String[] NUMERICMONTHS = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
	
	public static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	public static final SimpleDateFormat UNIFORM_DATE_TIME = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	public static final SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat REVERSE_STANDARD = new SimpleDateFormat("yyyy/MM/dd");
	public static final SimpleDateFormat SHORT_FORMAT = new SimpleDateFormat("MM/dd/yy");
	public static final SimpleDateFormat DASH_FORMAT = new SimpleDateFormat("MM-dd-yy");
	public static final SimpleDateFormat UNDELIMITED_FORMAT = new SimpleDateFormat("MMddyyyy");
	
	private String zeroValue;
	private String maxValue;

	public CustomDateFormat() {
		super();
		this.zeroValue = "None";
		this.maxValue = "Pending";
	}
	public CustomDateFormat(String pattern) {	
		this (pattern, "None");
	}
	public CustomDateFormat(String pattern, String zeroValue) {
		this (pattern, zeroValue, "Pending");
	}
	public CustomDateFormat(String pattern, String zeroValue, String maxValue) {
		super (pattern);
		this.zeroValue = zeroValue;
		this.maxValue = maxValue;
	}
	// Formats the given Date into a date/time string and appends the result to the given StringBuffer.
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		if (date.getTime() == 0) {
			return new StringBuffer(zeroValue);
		} else if (date.getTime() == Long.MAX_VALUE) {
			return new StringBuffer(maxValue);
		} else {
			return super.format(date, toAppendTo, pos);
		}
	}
	//Parses text from a string to produce a Date.
	public Date parse(String text, ParsePosition pos) {
		if (text.equals(zeroValue)) {
			return new Date(0);
		} else if (text.equals(maxValue)) {
			return new Date(Long.MAX_VALUE);
		}
		return super.parse(text, pos);
	}
	// Attempt to parse a date from some input text agnostically by detecting some of the the most commonly used formats
	public static Date getDate(String input) {
		try {
			if (input.contains(".")) {
				long time = (long)((NumberFormat.getInstance().parse(input).doubleValue()-25568.0)*86400000.0-68400000.0);
				return new Date(time);
			} else if (input.equals("None")) {
				return new Date(0);
			} else if (input.equals("Pending")) {
				return new Date(Long.MAX_VALUE);
			} else if (input.equals("Sun Aug 17 01:12:55 CST 292278994")) {
				return new Date(Long.MAX_VALUE);
			} else if (input.length() == 28) {
				return FULL_FORMAT.parse(input);
			} else if (input.length() == 19) {
				return UNIFORM_DATE_TIME.parse(input);
			} else if (input.length() == 10) {
				return STANDARD_FORMAT.parse(input);
			} else if (input.length() == 8) {
				if (input.contains("/")) {
					return SHORT_FORMAT.parse(input);
				} else if (input.contains("-")) {
					return DASH_FORMAT.parse(input);
				} else {
					return UNDELIMITED_FORMAT.parse(input);
				}
			} else {
				return DateFormat.getDateInstance().parse(input);
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Unable to parse date from text '" + input + "' with error message: " + ex.getMessage());
			return new Date(0);
		}
	}
	// Parses a date from some input text and returns a date object with any time of day parameters cleared
	public static Date getTimelessDate(String input) {
		return getTimelessDate(getDate(input));
	}
	// Determine the number of days between two dates
	public static int getDaysOfSeperation(Calendar calendarA, Calendar calendarB) {
		int years = getYearsOfSeperation(calendarA, calendarB);
		return (years * 365) + (calendarA.get(java.util.Calendar.DAY_OF_YEAR) - calendarB.get(java.util.Calendar.DAY_OF_YEAR));
	}
	// Determine the number of months between two dates
	public static int getMonthsOfSeperation(Calendar calendarA, Calendar calendarB) {
		int years = getYearsOfSeperation(calendarA, calendarB);
		return (years * 12) + (calendarA.get(java.util.Calendar.MONTH) - calendarB.get(java.util.Calendar.MONTH));
	}
	// Determine the number of years between two dates
	public static int getYearsOfSeperation(Calendar calendarA, Calendar calendarB) {
		return calendarA.get(java.util.Calendar.YEAR) - calendarB.get(java.util.Calendar.YEAR);
	}
	// Get the last day value in a given month and year
	public static int getLastDay(int year, int month) {
		Calendar cal = java.util.Calendar.getInstance();
		cal.set(year, month, 1);
		return cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
	}
	// Get a date value for the first day in a given month and year
	public static Date getFirstDate(int year, int month) {
		try {
			return new SimpleDateFormat("M/d/yyyy").parse(NUMERICMONTHS[month] + "/1/" + year);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Unable to determine first date for year=" + year + " month=" + month + " with error message: " + ex.getMessage());
			return new Date(0);
		}
	}
	// Get a date value for the last day in a given month and year
	public static Date getLastDate(int year, int month) {
		try {
			return new SimpleDateFormat("M/d/yyyy").parse(NUMERICMONTHS[month] + "/" + getLastDay(year, month) + "/" + year);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Unable to determine last date for year=" + year + " month=" + month + " with error message: " + ex.getMessage());
			return new Date(0);
		}
	}
	// Clears all time values from a date leaving just the day, month, and year
	public static Date getTimelessDate(Date source) {
		Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(source);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	// Compare two date values not factoring time of day
	public static int compareTimeless(Date a,  Date b) {
		return getTimelessDate(a).compareTo(getTimelessDate(b));
	}
	// Determine if the date is in a valid range
	public static boolean isValid(Date source) {
		return source.getTime() > 0 && source.getTime() < Long.MAX_VALUE;
	}
}