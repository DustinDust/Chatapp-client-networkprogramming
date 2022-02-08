package com.newclient.utils;

import java.util.HashMap;

/**
 * Utilities class used to return the detail message string in accordance to 
 * the code passed as param
 */
public class ResCode {
	static HashMap<String, String> resCodeToString = new HashMap<>();
	static {
		resCodeToString.put("11", "Credential Error");
		resCodeToString.put("12", "Server Error");
		resCodeToString.put("13", "Account is already logged in");
		resCodeToString.put("21", "User is current offline. Please reset your user list");
		resCodeToString.put("31", "Group name already exist");
		resCodeToString.put("61", "Session Expired");
	}

	/**
	 * 
	 * @param code the reponse code in digit
	 * @return the detailed String message.
	 */
	public static String get(String code) {
		if (!resCodeToString.containsKey(code)) {
			return "Undefined Error Code: " + code;
		} else {
			return resCodeToString.get(code);
		}
	}
}
