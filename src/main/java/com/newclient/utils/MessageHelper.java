package com.newclient.utils;

import java.util.ArrayList;
import java.util.Arrays;

import com.newclient.models.MessageType;

/**
 * Utilities Class that provides utilities functionalities for composing, destructuring, classifying message
 */
public class MessageHelper {
	// some constant
	public static final String endingDelimiter = "\r\n";
	public static String l2Delimiter;
	public static String l3Delimiter;
	static {
		char[] c = { (char) 6 };
		char[] d = { (char) 17 };
		l2Delimiter = new String(c);
		l3Delimiter = new String(d);
	}

	/**
	 * Compose Login message to send
	 * @param username user's entered user
	 * @param password user's entered password
	 * @return the message string 
	 */
	public static String composeLoginMessage(String username, String password) {
		return "USER" + l2Delimiter + username + l2Delimiter + password + endingDelimiter;
	}

	/**
	 * compose list user message 
	 * @param from current user
	 * @return the message string
	 */
	public static String composeListUserMessage(String from) {
		return "LISTUSER" + l2Delimiter + from + endingDelimiter;
	}

	/**
	 * compose list group message
	 * @param from current user
	 * @return the message string
	 */
	public static String composeListGroupMessage(String from) {
		return "LISTGROUP" + l2Delimiter + from + endingDelimiter;
	}

	/**
	 * create group message to create a group
	 * @param from current user
	 * @param groupname desired group name
	 * @param userList list of the initial members of the group
	 * @return: composed message string
	 */
	public static String composeCreateGroupMessage(String from, String groupname, ArrayList<String> userList) {
		StringBuilder msg = new StringBuilder("GROUP" + l2Delimiter);
		msg.append(groupname + l2Delimiter + from);
		for (String user : userList) {
			msg.append(l2Delimiter).append(user);
		}
		msg.append(endingDelimiter);
		return msg.toString();
	}

	/**
	 * compose Post message to send message to another user
	 * @param from current user
	 * @param to target user
	 * @param what content of the message
	 * @return composed message string
	 */
	public static String composePostMessage(String from, String to, String what) {
		return "POST" + l2Delimiter + from + l2Delimiter + to + l2Delimiter + what + endingDelimiter;
	}

	/**
	 * compose Leave message send the leave group
	 * @param who current user
	 * @param whichgroup groupname to leave
	 * @return composed message string
	 */
	public static String composeLeaveGroupMessage(String who, String whichgroup) {
		return "LEAVE" + l2Delimiter + who + l2Delimiter + whichgroup + endingDelimiter;
	}

	/**
	 * compose quit message to logout
	 * @param who current user 
	 * @return composed message string
	 */
	public static String composeQuitMessage(String who) {
		return "QUIT" + l2Delimiter + who + endingDelimiter;
	}

	/**
	 * compose BroadCast message to send a message to a group
	 * @param from current user
	 * @param groupname target group
	 * @param message message content
	 * @return compose message string
	 */
	public static String composeBroadCaseMessage(String from, String groupname, String message) {
		return "BROADCAST" + l2Delimiter + from + l2Delimiter + groupname + l2Delimiter + message + endingDelimiter;
	}

	/**
	 * Compose add message to add a user to a group
	 * @param  name of the target group
	 * @param who user to add
	 * @param from current user
	 * @return composed message string
	 */
	public static String composeAddMessasge(String groupname, ArrayList<String> who, String from) {
		StringBuilder message = new StringBuilder("ADD" + l2Delimiter + from + l2Delimiter + groupname);
		for (String i : who) {
			message.append(l2Delimiter);
			message.append(i);
		}
		message.append(endingDelimiter);
		return message.toString();
	}

	/**
	 * Classify a received message to one of MessageType enum type
	 * @param message received message
	 * @return the MessageType type that indicate which type the message param is
	 */
	public static MessageType getMessageType(String message) {
		String[] pack = message.split(l2Delimiter);
		if (pack[0].equals("USER")) {
			return MessageType.USER;
		}
		if (pack[0].equals("GROUP")) {
			return MessageType.GROUP;
		}
		if (pack[0].equals("QUIT")) {
			return MessageType.QUIT;
		}
		if (pack[0].equals("ADD")) {
			return MessageType.ADD;
		}
		if (pack[0].equals("MESSAGEGROUP")) {
			return MessageType.MESSAGEGROUP;
		}
		if (pack[0].equals("MESSAGEUSER")) {
			return MessageType.MESSAGEUSER;
		}
		if (pack[0].equals("POST")) {
			return MessageType.POST;
		}
		if (pack[0].equals("LISTUSER")) {
			return MessageType.LISTUSER;
		}
		if (pack[0].equals("LISTGROUP")) {
			return MessageType.LISTGROUP;
		}
		if (pack[0].equals("LEAVE")) {
			return MessageType.LEAVE;
		}
		return MessageType.INVALID;
	}

	/**
	 * splite a message using l2Delimiter as a delimiter
	 * @param message the received message that needs splitting
	 * @return ArrayList of all the splitted part 
	 */
	public static ArrayList<String> splitMessageL2(String message) {
		String[] pack = message.split(l2Delimiter);
		ArrayList<String> content = new ArrayList<>(Arrays.asList(pack));
		return content;
	}

	/**
	 * splite a message using l3Delimiter as a delimiter
	 * @param message the received message that needs splitting
	 * @return ArrayList of all the splitted part 
	 */
	public static ArrayList<String> splitMessageL3(String message) {
		String[] pack = message.split(l3Delimiter);
		return new ArrayList<String>(Arrays.asList(pack));
	}

}
