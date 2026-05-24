package presentation.controller;

import app.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ClientHomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (MainApp.getCurrentUser() != null) {
            welcomeLabel.setText(
                    "Bienvenue, " + MainApp.getCurrentUser().getPrenom() + " " + MainApp.getCurrentUser().getNom());
        }
    }

    @FXML
    private void goToEvents() {
        ClientShellController.getInstance().showEvents();
    }

    @FXML
    private void showTickets() {
        ClientShellController.getInstance().showTickets();
    }

    @FXML
    private void showCart() {
        ClientShellController.getInstance().showCart();
    }

    @FXML
    private void showProfile() {
        ClientShellController.getInstance().showProfile();
    }
}
