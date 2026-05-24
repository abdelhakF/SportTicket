package presentation.controller;

import app.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ContactController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextArea messageArea;

    @FXML
    private void handleBack() {
        MainApp.setRoot("HomeView");
    }

    @FXML
    private void handleSendMessage() {
        // Logic to send message (mock)
        System.out.println("Message sent by " + nameField.getText());
        handleBack();
    }
}
