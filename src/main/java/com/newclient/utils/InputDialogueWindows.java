package com.newclient.utils;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This class helps spawn a dialogue window to get user input
 */
public class InputDialogueWindows {
	private String input;
	private Stage dstg;
	private Scene dscn;
	private TextField inputfield;
	private Button btn;
	private VBox container;

	/**
	 * 
	 * @param title title of the window
	 * @param message prompt text to the input field
	 */
	public InputDialogueWindows(String title, String message) {
		dstg = new Stage();
		dstg.initModality(Modality.APPLICATION_MODAL);
		dstg.setTitle(title);
		inputfield = new TextField();
		inputfield.setPromptText(message);
		this.btn = new Button("Confirm");
		btn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				input = inputfield.getText();
				inputfield.clear();
				dstg.close();
			};
		});
		container = new VBox(20, inputfield, btn);
		container.setAlignment(Pos.CENTER);
		dscn = new Scene(container, 400, 150);
		dstg.setScene(dscn);
	}

	/**
	 * Show dialouge and block the main GUI until the dialouge window is closed
	 */
	public void showAndWait() {
		this.dstg.showAndWait();
	}

	/**
	 * Get the data that user input
	 * @return user input
	 */
	public String getInput() {
		return this.input;
	}
}
