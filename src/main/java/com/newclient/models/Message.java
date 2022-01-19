package com.newclient.models;

public class Message {
	private String content;
	private String from; // who sent the message

	public Message(String from, String content) {
		this.from = new String(from);
		this.content = new String(content);
	}

	public String getContent() {
		return content;
	}

	public String fromWho() {
		return from;
	}

	public String getFormatted() {
		return "[" + from + "]: " + content + "\n";
	}

}
