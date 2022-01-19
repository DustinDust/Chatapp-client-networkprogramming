package com.newclient.utils;

import java.util.ArrayList;
import java.util.Arrays;

import com.newclient.models.MessageType;

// compose message string to be send to server
public class MessageHelper {
	public static final String endingDelimiter = "\r\n";
	public static String l2Delimiter;
	public static String l3Delimiter;
	static {
		char[] c = { (char) 6 };
		char[] d = { (char) 7 };
		l2Delimiter = new String(c);
		l3Delimiter = new String(d);
	}

	public static String composeLoginMessage(String username, String password) {
		return "USER" + l2Delimiter + username + l2Delimiter + password + endingDelimiter;
	}

	public static String composeListUserMessage(String from) {
		return "LISTUSER" + l2Delimiter + from + endingDelimiter;
	}

	public static String composeListGroupMessage(String from) {
		return "LISTGROUP" + l2Delimiter + from + endingDelimiter;
	}

	public static String composeCreateGroupMessage(String from, String groupname, ArrayList<String> userList) {
		StringBuilder msg = new StringBuilder("GROUP" + l2Delimiter);
		msg.append(groupname + l2Delimiter + from);
		for (String user : userList) {
			msg.append(l2Delimiter).append(user);
		}
		msg.append(endingDelimiter);
		return msg.toString();
	}

	public static String composePostMessage(String from, String to, String what) {
		return "POST" + l2Delimiter + from + l2Delimiter + to + l2Delimiter + what + endingDelimiter;
	}

	public static String composeLeaveGroupMessage(String who, String whichgroup) {
		return "LEAVE" + l2Delimiter + who + l2Delimiter + whichgroup + endingDelimiter;
	}

	public static String composeQuitMessage(String who) {
		return "QUIT" + l2Delimiter + who + endingDelimiter;
	}

	public static String composeBroadCaseMessage(String from, String groupname, String message) {
		return "BROADCASE" + l2Delimiter + from + l2Delimiter + groupname + l2Delimiter + message + endingDelimiter;
	}

	public static String composeAddMessasge(String groupname, ArrayList<String> who) {
		StringBuilder message = new StringBuilder("ADD" + l2Delimiter + groupname + l2Delimiter + who.get(0));
		for (String i : who) {
			message.append(l3Delimiter);
			message.append(i);
		}
		message.append(endingDelimiter);
		return message.toString();
	}

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

	public static ArrayList<String> splitMessageL2(String message) {
		String[] pack = message.split(l2Delimiter);
		ArrayList<String> content = new ArrayList<>(Arrays.asList(pack));
		return content;
	}

	public static ArrayList<String> splitMessageL3(String message) {
		String[] pack = message.split(l3Delimiter);
		return new ArrayList<String>(Arrays.asList(pack));
	}

}
