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
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
		mainStage.setScene(menu.getScene());
		mainStage.show();
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
						MessageDialogueWindows err = new MessageDialogueWindows("Failt to send message to server", "Error",
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
			menuScene = new Scene(container, 600, 600);
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
							MessageDialogueWindows err = new MessageDialogueWindows("Cant send message to the server", "Error",
									Modality.APPLICATION_MODAL);
							err.showDialogue();
							e.printStackTrace();
						});
					}
				} else {
					Platform.runLater(() -> {
						MessageDialogueWindows errorWD = new MessageDialogueWindows("Error Code: " + part.get(1), "Fail to login",
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
			listUsers.getItems().add("dummy 1");
			listUsers.getItems().add("dummy 2");
			listGroups.getItems().add("group dummy");
			groupReloadButton = new Button("Reload group List");
			userReloadButton = new Button("Reload user List");
			groupButton = new Button("Create a group from selected users");
			addUserButton = new Button("Add selected users to selected group");
			leaveGroupButton = new Button("Leave selected group");
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
							// TODO: move to chat scene with groups
						}
					}
				}
			});
			listUsers.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
						if (listUsers.getSelectionModel().getSelectedItem() != null) {
							// TODO: move to chat scene with user
						}
					}
				}
			});
			groupButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					ArrayList<String> selectedUsers = new ArrayList<>(listUsers.getSelectionModel().getSelectedItems());
					if (selectedUsers.size() <= 1) {
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
					if (selectedUsers.size() <= 0 || groupName == null) {
						MessageDialogueWindows err = new MessageDialogueWindows("Invalid Input", "Error",
								Modality.APPLICATION_MODAL);
						err.showDialogue();
						return;
					}
					String message = MessageHelper.composeAddMessasge(groupName, selectedUsers);
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
			listScene = new Scene(container, 600, 600);
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

		Consumer<String> getListGroupHandler() {
			return str -> {
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
}
