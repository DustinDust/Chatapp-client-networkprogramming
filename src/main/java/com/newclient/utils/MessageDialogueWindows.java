package com.newclient.utils;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MessageDialogueWindows {
	private Stage stage;
	private Scene scene;
	private VBox container;
	private Label message;
	private Button interact;

	public MessageDialogueWindows(String mes, String title, Modality modal) {
		message = new Label(mes);
		message.setFont(Font.font(16));
		interact = new Button("Close");
		interact.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					stage.close();
				}
			};
		});

		container = new VBox(20, message, interact);
		container.setAlignment(Pos.CENTER);
		scene = new Scene(container, 400, 150);
		stage = new Stage();
		stage.setTitle(title);
		stage.setScene(scene);
		stage.initModality(modal);
	}

	public void setMessage(String mes) {
		message.setText(mes);
	}

	public void setTitle(String title) {
		stage.setTitle(title);
	}

	public void showDialogue() {
		stage.show();
	}

}
