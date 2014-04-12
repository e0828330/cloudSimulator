package utils;

import java.util.Date;

public class Utils {
	/**
	 * Gets the current simulated date based on the current minute
	 * 
	 * @param minute
	 * @return
	 */
	public static Date getCurrentTime(int minute) {
		long start = 1356994800l; // 01.01.2013 00:00 
		return new Date((start + minute * 60) * 1000);
	}
}
