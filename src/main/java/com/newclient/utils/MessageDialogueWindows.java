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
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This class helps spawn a dialouge window to display some message
 */
public class MessageDialogueWindows {
	private Stage stage;
	private Scene scene;
	private VBox container;
	private Label message;
	private Button interact;

	/**
	 * 
	 * @param mes the message to display
	 * @param title the title of the dialouge window
	 * @param modal Modality Mode
	 */
	public MessageDialogueWindows(String mes, String title, Modality modal) {
		message = new Label(mes);
		message.setTextAlignment(TextAlignment.CENTER);
		message.setFont(Font.font(16));
		interact = new Button("Close");

		//event handler when close 
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
		scene = new Scene(container, 600, 200);
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

	/**
	 * show the dialogue window
	 */
	public void showDialogue() {
		stage.show();
	}

}
