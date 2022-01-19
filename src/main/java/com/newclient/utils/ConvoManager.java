package com.newclient.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.newclient.models.Conversation;
import com.newclient.models.Message;

public class ConvoManager {
	private HashMap<String, Conversation> UserConvos;
	private HashMap<String, Conversation> GroupConvos;
	private String activeConvo;
	private boolean isUser;

	public ConvoManager() {
		UserConvos = new HashMap<>();
		GroupConvos = new HashMap<>();
		// no active conversation when initiated;
		activeConvo = new String("");
	}

	public void activateConvo(String name, boolean isUSer) {
		this.isUser = isUSer;
		this.activeConvo = name;
	}

	public Conversation getActivateConvo() {
		if (isUser) {
			return UserConvos.get(activeConvo);
		} else {
			return GroupConvos.get(activeConvo);
		}
	}

	public void deactivateConvo() {
		activeConvo = "";
	}

	public void updateConvos(ArrayList<Conversation> userList, boolean isUser) {
		if (isUser) {
			for (Conversation i : userList) {
				if (UserConvos.containsKey(i.getName())) {
					UserConvos.get(i.getName()).updateUsers(i.getUsers());
				} else {
					UserConvos.put(i.getName(), i);
				}
			}
		} else {
			for (Conversation i : userList) {
				if (GroupConvos.containsKey(i.getName())) {
					GroupConvos.get(i.getName()).updateUsers(i.getUsers());
				} else {
					GroupConvos.put(i.getName(), i);
				}
			}
		}
	}

	public void insertMessage(Message mes, String which, boolean isUser) {
		if (isUser) {
			if (!UserConvos.containsKey(which)) {
				UserConvos.put(which, new Conversation(new ArrayList<>(), which));
			}
			UserConvos.get(which).addMessage(mes);
		} else {
			if (!UserConvos.containsKey(which)) {
				UserConvos.put(which, new Conversation(new ArrayList<>(), which));
			}
			UserConvos.get(which).addMessage(mes);
		}
	}

	public ArrayList<Message> getBuffer() {
		if (this.isUser) {
			return UserConvos.get(this.activeConvo).getBuffer();
		} else {
			return GroupConvos.get(this.activeConvo).getBuffer();
		}
	}

	public ArrayList<Message> getBuffer(String fromWho, boolean isUser) {
		if (isUser) {
			return UserConvos.get(fromWho).getBuffer();
		} else {
			return GroupConvos.get(fromWho).getBuffer();
		}
	}

	public void clearBuffer(String which, boolean isUser) {
		if (isUser) {
			UserConvos.get(which).getBuffer().clear();
		} else {
			GroupConvos.get(which).getBuffer().clear();
		}
	}
}
