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

public class InputDialogueWindows {
	private String input;
	private Stage dstg;
	private Scene dscn;
	private TextField inputfield;
	private Button btn;
	private VBox container;

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

	public void showAndWait() {
		this.dstg.showAndWait();
	}

	public String getInput() {
		return this.input;
	}
}
