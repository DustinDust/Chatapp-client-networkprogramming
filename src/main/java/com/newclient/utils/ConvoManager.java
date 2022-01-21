package com.newclient.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.newclient.models.Conversation;
import com.newclient.models.Message;

public class ConvoManager {
	private HashMap<String, Conversation> UserConvos;
	private HashMap<String, Conversation> GroupConvos;

	public ConvoManager() {
		UserConvos = new HashMap<>();
		GroupConvos = new HashMap<>();
	}

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

	public ArrayList<String> getMembers(String convo, boolean isUser) {
		if (isUser) {
			return UserConvos.get(convo).getUsers();
		} else {
			return GroupConvos.get(convo).getUsers();
		}
	}

}
