package com.newclient.models;

/**
 * Define a Message model
 */
public class Message {
	private String content;
	private String from; // who sent the message

	/**
	 * 
	 * @param from the user that send this message
	 * @param content the text content of the message
	 */
	public Message(String from, String content) {
		this.from = new String(from);
		this.content = new String(content);
	}

	/**
	 * 
	 * @return The content of the message
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 
	 * @return get the username of the user that sent the message
	 */
	public String fromWho() {
		return from;
	}

	/**
	 * 
	 * @return the formatted string of the message, ready to be used
	 */
	public String getFormatted() {
		return "[" + from + "]: " + content + "\n";
	}

}
