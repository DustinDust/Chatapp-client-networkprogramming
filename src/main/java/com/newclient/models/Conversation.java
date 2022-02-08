package com.newclient.models;

import java.util.ArrayList;

/**
 * Define a Conversation class
 */
public class Conversation {
	private String name;
	private ArrayList<String> users; // members of the conversation
	private ArrayList<Message> buffer; // List of Message object of every message that have been sent in this conversation

	/**
	 * 
	 * @param users list of members in the conversation
	 * @param name name of the conversation, if it's just a user conversation, then the name should be the name of the user
	 */
	public Conversation(ArrayList<String> users, String name) {
		this.name = name;
		this.users = users;
		buffer = new ArrayList<>();
	}

	/**
	 * 
	 * @return name of the conversation
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @param users new user list, if the new user list is empty the clears the buffer (basically reset the conversation)
	 */
	public void updateUsers(ArrayList<String> users) {
		if (users.size() == 0) {
			buffer.clear();
		}
		this.users = users;
	}

	/**
	 * 
	 * @return user list of the conversation
	 */
	public ArrayList<String> getUsers() {
		return users;
	}

	/**
	 * 
	 * add a mesasge to the Buffer of the conversation
	 * 
	 * @param mess Message Object ot be added to the buffer of the converstaion
	 */
	public void addMessage(Message mess) {
		buffer.add(mess);
	}

	/**
	 * 
	 * 
	 * @return the buffer of the conversation, which contains Message objects of every message that have been sent in this conversation
	 */
	public ArrayList<Message> getBuffer() {
		return this.buffer;
	}

}
