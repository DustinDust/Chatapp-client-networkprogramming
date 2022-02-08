package com.newclient;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.newclient.models.Conversation;
import com.newclient.models.Message;
import com.newclient.utils.ConvoManager;
import com.newclient.utils.InputDialogueWindows;
import com.newclient.utils.MessageDialogueWindows;
import com.newclient.utils.MessageHelper;

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
 */
public class App extends Application {
	Stage mainStage = new Stage();
	String currentUser;
	Menu menu = new Menu();
	ConversationList convoList = new ConversationList();
	ClientConnection cc = new ClientConnection();
	ConvoManager cm = new ConvoManager();
	ChatView chatview = new ChatView();

	@Override
	public void init() throws Exception {
		super.init();
		cc.startConnection();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		cc.closeConnection();
	}

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

	public static void main(String[] args) {
		launch();
	}

	private class Menu {
		private VBox container;
		private Button loginConfirmButton;
		private Label loginLabel;
		private TextField loginUserInput;
		private PasswordField loginPassInput;
		private Scene menuScene;

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
						MessageDialogueWindows errorWD = new MessageDialogueWindows("Error" + part.get(1), "Fail to login",
								Modality.APPLICATION_MODAL);
						errorWD.showDialogue();
					});
				}
			};
		}
	}

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
					}
				}
			});
			groupButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					ArrayList<String> selectedUsers = new ArrayList<>(listUsers.getSelectionModel().getSelectedItems());
					if (selectedUsers.size() < 1) {
						return;
					}
					InputDialogueWindows getGroupName = new InputDialogueWindows("Input", "group name go here");
					getGroupName.showAndWait();
					String groupname = getGroupName.getInput();
					if (groupname.length() <= 0) {
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
						MessageDialogueWindows err = new MessageDialogueWindows("Invalid Input", "Error",
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

		void updateUserList(ArrayList<String> users) {
			listUsers.getItems().setAll(users);
		}

		void updateGroupList(ArrayList<String> groups) {
			listGroups.getItems().setAll(groups);
		}

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
						MessageDialogueWindows error = new MessageDialogueWindows("Fail to logout: " + parts.get(1), "Oops!",
								Modality.APPLICATION_MODAL);
						error.showDialogue();
					});
					return;
				}
			};
		}

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
						MessageDialogueWindows error = new MessageDialogueWindows("Fail to leave group", "Error",
								Modality.APPLICATION_MODAL);
						error.showDialogue();
					});
				}
			};
		}

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

		Consumer<String> getAddHandler() {
			return (str) -> {
				ArrayList<String> parts = MessageHelper.splitMessageL2(str);
				if (parts.get(1).equals("40")) {
					try {
						cc.send(MessageHelper.composeListGroupMessage(currentUser));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows dw = new MessageDialogueWindows("Error adding user: " + parts.get(2),
								"Fail to add users",
								Modality.APPLICATION_MODAL);
						dw.showDialogue();
					});
				}
			};
		}

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
					MessageDialogueWindows dw = new MessageDialogueWindows("Error creating GROUP: " + parts.get(2),
							"Fail to create group",
							Modality.APPLICATION_MODAL);
					dw.showDialogue();
				}
			};
		}
	}

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
							sendmessage = MessageHelper.composeBroadCaseMessage(currentUser, currentConvo, message);
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

		Consumer<String> getPostHandler() {
			return str -> {
				ArrayList<String> part = MessageHelper.splitMessageL2(str);
				if (part.get(1).equals("21")) {
					Platform.runLater(() -> {
						MessageDialogueWindows error = new MessageDialogueWindows(
								"Fail to send message: Your message partner had gone offline...", "Error", Modality.APPLICATION_MODAL);
						error.showDialogue();
					});
				} else
					return;
			};
		}
	}
}
