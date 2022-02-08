package com.newclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

import com.newclient.models.MessageType;
import com.newclient.utils.MessageHelper;

/**
 * This class will handle all the socket connection logic, including sending message,
 * receiving message then call the right callback function for each message type
 */
public class ClientConnection {
	private ConnectionThread conThread = new ConnectionThread("127.0.0.1", 5500);

	/**
	 * Lis of call back function for each message type
	 */
	Consumer<String> onLoginCallback;
	Consumer<String> onMessageUserCallback;
	Consumer<String> onListUserCallback;
	Consumer<String> onListGroupCallback;
	Consumer<String> onGroupCallback;
	Consumer<String> onLogoutCallback;
	Consumer<String> onAddCallback;
	Consumer<String> onLeaveGroupCallback;
	Consumer<String> onPostCallback;
	Consumer<String> onMessageGroupCallback;

	/**
	 * Begin connection
	 * @throws Exception when cant connect to the server, persumably because there's no server or the server
	 * deny the connection
	 */
	public void startConnection() throws Exception {
		conThread.start();
	}

	/**
	 * 
	 * send a message to server
	 * 
	 * @param message message string
	 * @throws Exception when the print method the the print stream associate with the connection failed
	 */
	public void send(String message) throws Exception {
		conThread.out.print(message);
		conThread.out.flush();
	}

	/**
	 * Close the connection gracefully
	 * @throws Exception when an IO error occured while closing the socket
	 */
	public void closeConnection() throws Exception {
		conThread.con.close();
	}

	/**
	 * 
	 * Setters for all the callback, will be used in the main App class
	 */

	/**
	* 
	 */
	public void setOnGroupCallback(Consumer<String> onGroupCallback) {
		this.onGroupCallback = onGroupCallback;
	}

	public void setOnListUserCallback(Consumer<String> func) {
		this.onListUserCallback = func;
	}

	public void setOnPostCallback(Consumer<String> func) {
		this.onPostCallback = func;
	}

	public void setOnMessageGroupCallback(Consumer<String> onMessageGroupCallback) {
		this.onMessageGroupCallback = onMessageGroupCallback;
	}

	public void setOnLogoutCallback(Consumer<String> func) {
		this.onLogoutCallback = func;
	}

	public void setOnAddCallback(Consumer<String> func) {
		this.onAddCallback = func;
	}

	public void setOnLeaveGroupCallback(Consumer<String> func) {
		this.onLeaveGroupCallback = func;
	}

	public void setOnListGroupCallback(Consumer<String> func) {
		this.onListGroupCallback = func;
	}

	public void setOnLoginCallback(Consumer<String> func) {
		this.onLoginCallback = func;
	}

	public void setOnMessageUserCallback(Consumer<String> onMessageUserCallback) {
		this.onMessageUserCallback = onMessageUserCallback;
	}

	/**
	 * This class defined new thread running so that the receiving message procedures don't block the main
	 * thread from performing all other GUI action 
	 */
	private class ConnectionThread extends Thread {

		private Socket con;
		private PrintWriter out;
		private String ip;
		private int port;

		/**
		 * 
		 * @param ip ip of the server
		 * @param port port of the server
		 */
		ConnectionThread(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		/**
		 * start connection and begin waiting to receive message
		 */
		@Override
		public void run() {
			try (
					Socket con = new Socket(ip, port);
					PrintWriter out = new PrintWriter(con.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				this.out = out;
				this.con = con;
				con.setTcpNoDelay(true);

				while (true) {
					String message = in.readLine();
					System.out.println(message);
					MessageType type = MessageHelper.getMessageType(message);
					// Check type and call the right callback
					switch (type) {
						case USER:
							onLoginCallback.accept(message);
							break;
						case POST:
							onPostCallback.accept(message);
							break;
						case LISTUSER:
							onListUserCallback.accept(message);
							break;
						case LISTGROUP:
							onListGroupCallback.accept(message);
							break;
						case GROUP:
							onGroupCallback.accept(message);
							break;
						case MESSAGEUSER:
							onMessageUserCallback.accept(message);
							break;
						case MESSAGEGROUP:
							onMessageGroupCallback.accept(message);
							break;
						case ADD:
							onAddCallback.accept(message);
							break;
						case QUIT:
							onLogoutCallback.accept(message);
							break;
						case LEAVE:
							onLeaveGroupCallback.accept(message);
						case INVALID:
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
