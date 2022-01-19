package com.newclient.models;

import java.util.ArrayList;

public class Conversation {
	private String name;
	private ArrayList<String> users;
	private ArrayList<Message> buffer;

	public Conversation(ArrayList<String> users, String name) {
		this.name = name;
		this.users = users;
		buffer = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void updateUsers(ArrayList<String> users) {
		if (users.size() == 0) {
			buffer.clear();
		}
		this.users = users;
	}

	public ArrayList<String> getUsers() {
		return users;
	}

	public void addMessage(Message mess) {
		buffer.add(mess);
	}

	public ArrayList<Message> getBuffer() {
		return this.buffer;
	}

}
