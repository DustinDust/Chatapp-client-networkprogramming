package com.newclient.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.newclient.models.Conversation;
import com.newclient.models.Message;

/**
 * This class helps simplify the management of Alot of conversation object
 */
public class ConvoManager {
	// store the convos using a map of convoName - convo Object
	private HashMap<String, Conversation> UserConvos;
	private HashMap<String, Conversation> GroupConvos;

	public ConvoManager() {
		UserConvos = new HashMap<>();
		GroupConvos = new HashMap<>();
	}

	/**
	 * Update the the conversation current in data.
	 * 
	 * This function iterate through the param convoList, update the user list of conversation which 
	 * are already exist, add new conversation otherwise
	 * 
	 * @param convoList List of conversation object 
	 * @param isUser indicate which one should be updated group or user conversation data or 
	 * 
	 * 
	 */
	public void updateConvos(ArrayList<Conversation> convoList, boolean isUser) {
		if (isUser) {
			for (Conversation i : convoList) {
				if (UserConvos.containsKey(i.getName())) {
					UserConvos.get(i.getName()).updateUsers(i.getUsers());
				} else {
					UserConvos.put(i.getName(), i);
				}
			}
		} else {
			for (Conversation i : convoList) {
				if (GroupConvos.containsKey(i.getName())) {
					GroupConvos.get(i.getName()).updateUsers(i.getUsers());
				} else {
					GroupConvos.put(i.getName(), i);
				}
			}
		}
	}

	/**
	 * 
	 * Insert desired Message Object into the message buffer of desired conversation with key of {which} 
	 * Will create new conversation if the {which} conversation doesn't exist in stored data y
	 * 
	 * @param mes the {Message} object that needs to be insert
	 * @param which Key to identify the conversation from the Data
	 * @param isUser indicate whether the conversation is a group or a user conversation
	 * 
	 */
	public void insertMessage(Message mes, String which, boolean isUser) {
		if (isUser) {
			if (!UserConvos.containsKey(which)) {
				UserConvos.put(which, new Conversation(new ArrayList<>(), which));
			}
			UserConvos.get(which).addMessage(mes);
		} else {
			if (!GroupConvos.containsKey(which)) {
				GroupConvos.put(which, new Conversation(new ArrayList<>(), which));
			}
			GroupConvos.get(which).addMessage(mes);
		}
	}

	/**
	 * 
	 * @param fromWho key (name) of the conversation of which buffer is needed
	 * @param isUser indicate the type of the conversation
	 * @return ArrayList of Message Object that is the buffer of the conversation
	 */
	public ArrayList<Message> getBuffer(String fromWho, boolean isUser) {
		if (isUser) {
			return UserConvos.get(fromWho).getBuffer();
		} else {
			return GroupConvos.get(fromWho).getBuffer();
		}
	}

	/**
	 * 
	 * This function clears the message Buffer of a desired Conversation
	 * 
	 * @param which key (name) of the conversation of which buffer needs cleaning
	 * @param isUser indicates the type of the conversation
	 * 
	 */
	public void clearBuffer(String which, boolean isUser) {
		if (isUser) {
			UserConvos.get(which).getBuffer().clear();
		} else {
			GroupConvos.get(which).getBuffer().clear();
		}
	}

	/**
	 * 
	 * this function retrieves the user list of a desired conversation
	 * 
	 * @param convo key (name) of the conversation of which the user list is needed
	 * @param isUser indicates the type of the conversation
	 * @return ArrayList of Username which is the user list of the desired conversation
	 * 
	 */
	public ArrayList<String> getMembers(String convo, boolean isUser) {
		if (isUser) {
			return UserConvos.get(convo).getUsers();
		} else {
			return GroupConvos.get(convo).getUsers();
		}
	}

}
