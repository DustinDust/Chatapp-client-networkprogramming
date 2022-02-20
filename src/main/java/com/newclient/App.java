package com.newclient;

// general java import
import java.util.ArrayList;
import java.util.function.Consumer;

// project defined import
import com.newclient.models.Conversation;
import com.newclient.models.Message;
import com.newclient.utils.ConvoManager;
import com.newclient.utils.InputDialogueWindows;
import com.newclient.utils.MessageDialogueWindows;
import com.newclient.utils.MessageHelper;
import com.newclient.utils.ResCode;

// javafx GUI import
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX App
 * 
 * This class contain all the GUI logic and how GUI interact with data
 */
public class App extends Application {
	Stage mainStage = new Stage();
	String currentUser;
	Menu menu = new Menu();
	ConversationList convoList = new ConversationList();
	ClientConnection cc = new ClientConnection();
	ConvoManager cm = new ConvoManager();
	ChatView chatview = new ChatView();

	/**
	 * this method runs when the class is initialized before starting the GUI app
	 */
	@Override
	public void init() throws Exception {
		super.init();
		cc.startConnection();
	}

	/**
	 * This method runs before the app close. 
	 * Close the socket and exit GUI gracefully
	 */
	@Override
	public void stop() throws Exception {
		super.stop();
		cc.closeConnection();
	}

	/**
	 * Main GUI App starting from here
	 */
	@Override
	public void start(Stage stage) {
		cc.setOnLoginCallback(menu.getLoginMessageHandler());
		cc.setOnLogoutCallback(convoList.getLogoutHandler());
		cc.setOnAddCallback(convoList.getAddHandler());
		cc.setOnGroupCallback(convoList.getGroupHandler());
		cc.setOnListUserCallback(convoList.getListUserHandler());
		cc.setOnListGroupCallback(convoList.getListGroupHandler());
		cc.setOnLeaveGroupCallback(convoList.getLeaveGroupHandler());
		cc.setOnMessageUserCallback(chatview.getMessageUserHandler());
		cc.setOnMessageGroupCallback(chatview.getMessageGroupHandler());
		cc.setOnPostCallback(chatview.getPostHandler());
		mainStage.setScene(menu.getScene());
		mainStage.setResizable(false);
		mainStage.setTitle("Chat app client");
		mainStage.show();
		mainStage.setOnHiding((event) -> {
			try {
				cc.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * java launch
	 */
	public static void main(String[] args) {
		launch();
	}

	/**
	 * Login Menu class and all the login logic is handled here
	 */
	private class Menu {
		//GUI components
		private VBox container;
		private Button loginConfirmButton;
		private Label loginLabel;
		private TextField loginUserInput;
		private PasswordField loginPassInput;
		private Scene menuScene;

		// username that the user attempted to login as
		private String attemptedUser;

		Menu() {
			loginUserInput = new TextField();
			loginUserInput.setPromptText("Enter username");
			loginUserInput.setMaxWidth(200);
			loginPassInput = new PasswordField();
			loginPassInput.setPromptText("Enter password");
			loginPassInput.setMaxWidth(200);
			loginLabel = new Label("Login");
			loginLabel.setFont(Font.font(20));
			loginConfirmButton = new Button("Confirm");
			loginConfirmButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					attemptedUser = loginUserInput.getText();
					String attemptedPassword = loginPassInput.getText();
					if (attemptedUser.length() <= 0 || attemptedPassword.length() <= 0) {
						loginUserInput.clear();
						loginPassInput.clear();
						return;
					}
					try {
						cc.send(MessageHelper.composeLoginMessage(attemptedUser, attemptedPassword));
					} catch (Exception e) {
						MessageDialogueWindows err = new MessageDialogueWindows("Failed to send message to server", "Error",
								Modality.APPLICATION_MODAL);
						err.showDialogue();
						e.printStackTrace();
					}
					loginUserInput.clear();
					loginPassInput.clear();
				}
			});
			container = new VBox(20, loginLabel, loginUserInput, loginPassInput, loginConfirmButton);
			container.setAlignment(Pos.CENTER);
			menuScene = new Scene(container, 600, 400);
		}

		Scene getScene() {
			return menuScene;
		}

		// Login handler callback. Will be called by the ClientConnection object when received a login reponse
		Consumer<String> getLoginMessageHandler() {
			return (str) -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.get(1).equals("10")) {
					currentUser = attemptedUser;
					Platform.runLater(() -> {
						mainStage.setScene(convoList.getScene());
					});
					try {
						cc.send(MessageHelper.composeListUserMessage(currentUser));
						cc.send(MessageHelper.composeListGroupMessage(currentUser));
					} catch (Exception e) {
						Platform.runLater(() -> {
							MessageDialogueWindows err = new MessageDialogueWindows("Can't send message to the server", "Error",
									Modality.APPLICATION_MODAL);
							err.showDialogue();
							e.printStackTrace();
						});
					}
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows errorWD = new MessageDialogueWindows("Error: " + ResCode.get(part.get(1)),
								"Fail to login",
								Modality.APPLICATION_MODAL);
						errorWD.showDialogue();
					});
				}
			};
		}
	}

	/**
	 * This class handle the main "hub", where user list, group list and all other utils functionalities 
	 * button such as reload user/group list, create group, add user to group, logout are displayed
	 * 
	 * User can, from here, select a conversation (with user or group) to move to the next Chat Window
	 * and start chatting
	 * */
	private class ConversationList {
		private ListView<String> listGroups;
		private ListView<String> listUsers;
		private Button groupReloadButton;
		private Button userReloadButton;
		private Button logoutButton;
		private Button groupButton;
		private Button addUserButton;
		private Button leaveGroupButton;
		private VBox container;
		private Scene listScene;

		public ConversationList() {
			this.listUsers = new ListView<>();
			this.listGroups = new ListView<>();
			groupReloadButton = new Button("Reload group List");
			userReloadButton = new Button("Reload user List");
			groupButton = new Button("Create a group from selected users");
			addUserButton = new Button("Add selected users to selected group");
			leaveGroupButton = new Button("Leave selected group");

			/**
			 * Adding all the event handler when click to all the button
			 */
			leaveGroupButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY) {
						if (listGroups.getSelectionModel().getSelectedItem() != null) {
							String sgrup = listGroups.getSelectionModel().getSelectedItem();
							if (sgrup.length() <= 0)
								return;
							String message = MessageHelper.composeLeaveGroupMessage(currentUser, sgrup);
							try {
								cc.send(message);
							} catch (Exception e) {
								MessageDialogueWindows ew = new MessageDialogueWindows("Fail to send", "Error",
										Modality.APPLICATION_MODAL);
								ew.showDialogue();
								e.printStackTrace();
							}
						}
					}
				}
			});

			groupReloadButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY) {
						try {
							cc.send(MessageHelper.composeListGroupMessage(currentUser));
						} catch (Exception e) {
							MessageDialogueWindows err = new MessageDialogueWindows("Failed to send to server", "Error",
									Modality.APPLICATION_MODAL);
							err.showDialogue();
							e.printStackTrace();
						}
					}
				}
			});
			userReloadButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY) {
						try {
							cc.send(MessageHelper.composeListUserMessage(currentUser));
						} catch (Exception e) {
							MessageDialogueWindows err = new MessageDialogueWindows(
									"Failed to send to server: " + e.getLocalizedMessage(), "Error", Modality.APPLICATION_MODAL);
							err.showDialogue();
							e.printStackTrace();
						}
					}
				}
			});
			logoutButton = new Button("Log out");
			logoutButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY) {
						try {
							cc.send(MessageHelper.composeQuitMessage(currentUser));
						} catch (Exception e) {
							MessageDialogueWindows err = new MessageDialogueWindows(
									"Failed to send to server: " + e.getLocalizedMessage(), "Error", Modality.APPLICATION_MODAL);
							err.showDialogue();
							e.printStackTrace();
						}
					}
				};
			});
			listGroups.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
						if (listGroups.getSelectionModel().getSelectedItem() != null) {
							String selectedGroup = listGroups.getSelectionModel().getSelectedItem();
							chatview.activate(selectedGroup, false);
						}
					} else if (event.getButton() == MouseButton.SECONDARY) {
						listGroups.getSelectionModel().clearSelection();
					}
				}
			});
			listUsers.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
						if (listUsers.getSelectionModel().getSelectedItem() != null) {
							String selectedUser = listUsers.getSelectionModel().getSelectedItem();
							chatview.activate(selectedUser, true);
						}
					} else if (event.getButton() == MouseButton.SECONDARY) {
						listUsers.getSelectionModel().clearSelection();
					}
				}
			});
			groupButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					ArrayList<String> selectedUsers = new ArrayList<>(listUsers.getSelectionModel().getSelectedItems());
					if (selectedUsers.size() < 1 || selectedUsers == null) {
						MessageDialogueWindows error = new MessageDialogueWindows("Select at least one user", "Can't create group",
								Modality.APPLICATION_MODAL);
						error.showDialogue();
						return;
					}
					InputDialogueWindows getGroupName = new InputDialogueWindows("Input group name", "Fancy group");
					getGroupName.showAndWait();
					String groupname = getGroupName.getInput();
					if (groupname.length() <= 0 || groupname == null) {
						MessageDialogueWindows error = new MessageDialogueWindows("Group name can't be empty", "Error",
								Modality.APPLICATION_MODAL);
						error.showDialogue();
						return;
					}
					String groupMessage = MessageHelper.composeCreateGroupMessage(currentUser, groupname, selectedUsers);
					try {
						cc.send(groupMessage);
					} catch (Exception e) {
						MessageDialogueWindows error = new MessageDialogueWindows("Failed to send: " + e.getLocalizedMessage(),
								"Error", Modality.APPLICATION_MODAL);
						error.showDialogue();
						e.printStackTrace();
					}
				}
			});
			addUserButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					ArrayList<String> selectedUsers = new ArrayList<>(listUsers.getSelectionModel().getSelectedItems());
					String groupName = listGroups.getSelectionModel().getSelectedItem();
					if (selectedUsers == null || groupName == null || selectedUsers.size() <= 0) {
						MessageDialogueWindows err = new MessageDialogueWindows(
								"Invalid Input: at least one user and group must be selected",
								"Error",
								Modality.APPLICATION_MODAL);
						err.showDialogue();
						return;
					}
					String message = MessageHelper.composeAddMessasge(groupName, selectedUsers, currentUser);
					try {
						cc.send(message);
					} catch (Exception e) {
						MessageDialogueWindows err = new MessageDialogueWindows("Fail to send add request to server",
								"Error Adding Users", Modality.APPLICATION_MODAL);
						err.showDialogue();
						e.printStackTrace();
					}
					listUsers.getSelectionModel().clearSelection();
					listGroups.getSelectionModel().clearSelection();
				}
			});

			/**
			 * Putting all the component into window
			 */

			ButtonBar bar1 = new ButtonBar();
			ButtonBar bar2 = new ButtonBar();
			ButtonBar.setButtonData(logoutButton, ButtonData.RIGHT);
			ButtonBar.setButtonData(userReloadButton, ButtonData.LEFT);
			ButtonBar.setButtonData(groupReloadButton, ButtonData.LEFT);
			ButtonBar.setButtonData(groupButton, ButtonData.LEFT);
			ButtonBar.setButtonData(addUserButton, ButtonData.LEFT);
			ButtonBar.setButtonData(leaveGroupButton, ButtonData.LEFT);
			bar1.getButtons().addAll(userReloadButton, groupReloadButton, logoutButton);
			bar2.getButtons().addAll(groupButton, addUserButton, leaveGroupButton);
			listUsers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			container = new VBox(20, bar1, listUsers, bar2, listGroups);
			listScene = new Scene(container, 600, 400);
		}

		Scene getScene() {
			return this.listScene;
		}

		/**
		 * This function updates the listUsers component's data
		 * Will override all the current data
		 * @param users ArrayList of user name String 
		 */
		void updateUserList(ArrayList<String> users) {
			listUsers.getItems().setAll(users);
		}

		/**
		 * This function updates the listGroup component's data
		 * will override all the current data
		 * @param groups ArrayList of group name String
		 */
		void updateGroupList(ArrayList<String> groups) {
			listGroups.getItems().setAll(groups);
		}

		/**
		 * This function returen the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message
		 * @return A Consumer Object that acts as a callback function to be called to handle the
		 * 					LOGOUT reponse message 
		 */
		Consumer<String> getLogoutHandler() {
			return (str) -> {
				ArrayList<String> parts = MessageHelper.splitMessageL2(str);
				if (parts.get(1).equals("50")) {
					currentUser = "";
					Platform.runLater(() -> {
						mainStage.setScene(menu.getScene());
					});
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows error = new MessageDialogueWindows("Fail to logout", "Oops!",
								Modality.APPLICATION_MODAL);
						error.showDialogue();
					});
					return;
				}
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message.
		 * 
		 * this will handle both UI update and Data update
		 * @return A Consumer Object that acts as a callback function to handle the ListUser response message. 
		 * 
		 */
		Consumer<String> getListUserHandler() {
			return str -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.size() <= 1)
					return;
				ArrayList<String> actualUsers = new ArrayList<>();
				ArrayList<Conversation> userConvos = new ArrayList<>();
				for (int i = 1; i < part.size(); i++) {
					if (!part.get(i).equals(currentUser)) {
						actualUsers.add(part.get(i));
						ArrayList<String> iusers = new ArrayList<>();
						iusers.add(currentUser);
						iusers.add(part.get(i));
						Conversation tempConvo = new Conversation(iusers, part.get(i));
						userConvos.add(tempConvo);
					}
				}
				cm.updateConvos(userConvos, true);
				Platform.runLater(() -> {
					if (actualUsers.size() >= 1) {
						updateUserList(actualUsers);
					} else {
						updateUserList(new ArrayList<String>());
					}
				});
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message
		 * @return A Consumer Object that acts as a callback function to handle the LEAVE response message. 
		 */
		Consumer<String> getLeaveGroupHandler() {
			return str -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.get(1).equals("60")) {
					String leavewhich = part.get(2);
					cm.clearBuffer(leavewhich, false);
					try {
						cc.send(MessageHelper.composeListGroupMessage(currentUser));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows error = new MessageDialogueWindows(
								"Fail to leave group: " + ResCode.get(part.get(1)), "Error",
								Modality.APPLICATION_MODAL);
						error.showDialogue();
					});
				}
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message. 
		 * 
		 * Will handle both UI update and Conversation data update
		 * @return A Consumer Object that acts as a callback function to handle the LISTGROUP response message. 
		 * 
		 */
		Consumer<String> getListGroupHandler() {
			return str -> {
				ArrayList<String> part2 = MessageHelper.splitMessageL2(str);
				ArrayList<String> groupNames = new ArrayList<>();
				ArrayList<Conversation> groupConvos = new ArrayList<>();
				for (int i = 1; i < part2.size(); i++) {
					ArrayList<String> part3 = MessageHelper.splitMessageL3(part2.get(i));
					String groupName = part3.get(0);
					ArrayList<String> groupMembers = new ArrayList<>();
					for (int j = 1; j < part3.size(); j++) {
						groupMembers.add(part3.get(j));
					}
					Conversation groupconvo = new Conversation(groupMembers, groupName);
					groupNames.add(groupName);
					groupConvos.add(groupconvo);
				}
				cm.updateConvos(groupConvos, false);
				Platform.runLater(() -> {
					if (groupNames.size() >= 1) {
						updateGroupList(groupNames);
					} else {
						updateGroupList(new ArrayList<String>());
					}
				});
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message
		 * @return A Consumer Object that acts as a callback function to handle the ADD response message. 
		 * 
		 */
		Consumer<String> getAddHandler() {
			return (str) -> {
				ArrayList<String> parts = MessageHelper.splitMessageL2(str);
				if (parts.get(1).equals("40")) {
					StringBuilder listUserSuccessfullyAdded = new StringBuilder("");
					for (int i = 2; i < parts.size(); i++) {
						listUserSuccessfullyAdded.append("\n" + parts.get(i));
					}
					Platform.runLater(() -> {
						MessageDialogueWindows listadd = new MessageDialogueWindows(
								"List of Users added\n" + listUserSuccessfullyAdded,
								"Add response",
								Modality.APPLICATION_MODAL);
						listadd.showDialogue();
					});
					try {
						cc.send(MessageHelper.composeListGroupMessage(currentUser));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows dw = new MessageDialogueWindows(
								"Error adding user " + parts.get(1) + " " + ResCode.get(parts.get(1)),
								"Fail to add users",
								Modality.APPLICATION_MODAL);
						dw.showDialogue();
					});
				}
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message
		 * @return A Consumer Object that acts as a callback function to handle the GROUP response message. 
		 * 
		 */
		Consumer<String> getGroupHandler() {
			return (str) -> {
				ArrayList<String> parts = MessageHelper.splitMessageL2(str);
				if (parts.get(1).equals("30")) {
					try {
						cc.send(MessageHelper.composeListGroupMessage(currentUser));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows dw = new MessageDialogueWindows("Error: " + ResCode.get(parts.get(1)),
								"Fail to create group",
								Modality.APPLICATION_MODAL);
						dw.showDialogue();
					});
				}
			};
		}
	}

	/**
	 * Class contains UI for:
	 * 		Chat text, Message input, Conversation Info, sending message
	 * Also contains logic for receiving user message, sending message to another user/group
	 */
	private class ChatView {
		private Scene chatScene;
		private String currentConvo;
		private VBox container;
		private HBox infoDisplayContainer;
		private HBox inputDisplayContainer;
		private Button sendButton;
		private Button backButton;
		private boolean isUser;
		private TextArea chatText;
		private TextArea convoInfoText;
		private TextField messageInput;

		ChatView() {
			sendButton = new Button("Send");
			sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY) {
						String message = messageInput.getText();
						if (message.length() == 0) {
							return;
						}
						String sendmessage = MessageHelper.composePostMessage(currentUser, currentConvo, message);
						if (!isUser) {
							sendmessage = MessageHelper.composeBroadCastMessage(currentUser, currentConvo, message);
						}
						Message sent = new Message(currentUser, message);
						cm.insertMessage(sent, currentConvo, isUser);
						chatText.appendText(sent.getFormatted());
						try {
							cc.send(sendmessage);
							System.out.println(sendmessage);
						} catch (Exception e) {
							MessageDialogueWindows err = new MessageDialogueWindows("Fail to send message", "Error",
									Modality.APPLICATION_MODAL);
							err.showDialogue();
							e.printStackTrace();
						}
						messageInput.clear();
					}
				}
			});
			backButton = new Button("Back");
			backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY) {
						chatText.clear();
						convoInfoText.clear();
						currentConvo = "";
						mainStage.setScene(convoList.getScene());
					}
				}
			});
			chatText = new TextArea();
			chatText.setEditable(false);
			chatText.setPrefWidth(411);
			convoInfoText = new TextArea();
			convoInfoText.setEditable(false);
			convoInfoText.setPrefWidth(194.4);
			messageInput = new TextField();
			messageInput.setPromptText("Enter message");
			messageInput.setPrefWidth(509);
			infoDisplayContainer = new HBox(chatText, convoInfoText);
			inputDisplayContainer = new HBox(messageInput, sendButton, backButton);
			infoDisplayContainer.setPrefWidth(600);
			infoDisplayContainer.setPrefHeight(371);
			inputDisplayContainer.setPrefWidth(600);
			inputDisplayContainer.setPrefHeight(9);
			container = new VBox(infoDisplayContainer, inputDisplayContainer);
			chatScene = new Scene(container, 600, 400);
			currentConvo = "";
		}

		/**
		 * The method activates a user/group conversation, makes them the currently focused conversation (make the chatText display the conversation buffer, convoInfoText display
		 * info of the activated conversation)
		 * 
		 * @param which the conversation name (groupname/username)
		 * @param isUser is the conversation you desired to activate a group conversation or a user conversation
		 */
		public void activate(String which, boolean isUser) {
			currentConvo = which;
			this.isUser = isUser;
			ArrayList<String> members = cm.getMembers(which, isUser);
			ArrayList<Message> buffer = cm.getBuffer(which, isUser);
			StringBuilder infotext = new StringBuilder(which + " (");
			for (int i = 0; i < members.size() - 1; i++) {
				infotext.append(members.get(i) + ", ");
			}
			infotext.append(members.get(members.size() - 1) + ");");
			this.convoInfoText.setText(infotext.toString());
			StringBuilder chattext = new StringBuilder();
			for (Message chat : buffer) {
				chattext.append(chat.getFormatted());
			}
			this.chatText.setText(chattext.toString());
			mainStage.setScene(this.chatScene);
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message.
		 * 
		 * will only push the message to buffer if the target 
		 * conversation is different from current active conversation (which is being dispayed)
		 * @return A Consumer Object that acts as a callback function to handle the response message. 
		 */
		Consumer<String> getMessageUserHandler() {
			return str -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.size() >= 3) {
					String from = part.get(1);
					String content = part.get(2);
					Message received = new Message(from, content);
					cm.insertMessage(received, from, true);
					if (currentConvo.equals(from) && this.isUser) {
						chatText.appendText(received.getFormatted());
					}
				} else {
					return;
				}
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message.
		 * 
		 * will only push the message to buffer if the target 
		 * conversation is different from current active conversation (which is being dispayed)
		 * @return A Consumer Object that acts as a callback function to handle the MESSAGEGROUP message. 
		 * 
		 * 
		 */
		Consumer<String> getMessageGroupHandler() {
			return str -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.size() >= 4) {
					String groupname = part.get(1);
					String from = part.get(2);
					String content = part.get(3);
					Message received = new Message(from, content);
					cm.insertMessage(received, groupname, false);
					if (currentConvo.equals(groupname) && !this.isUser) {
						chatText.appendText(received.getFormatted());
					}
				} else {
					return;
				}
			};
		}

		/**
		 * This function return the Consumer Object within the context of the main App class
		 * so that the ClientConnection class can use as a callback to handle message
		 * @return A Consumer Object that acts as a callback function to handle the POST response message. 
		 * 
		 */
		Consumer<String> getPostHandler() {
			return str -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.get(1).equals("21")) {
					Platform.runLater(() -> {
						MessageDialogueWindows error = new MessageDialogueWindows(
								"Fail to send message: " + ResCode.get(part.get(1)), "Error", Modality.APPLICATION_MODAL);
						error.showDialogue();
					});
				} else
					return;
			};
		}
	}
}
