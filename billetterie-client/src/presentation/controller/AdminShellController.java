package presentation.controller;

import app.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminShellController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnUsers;
    @FXML
    private Button btnEvents;
    @FXML
    private Button btnStats;

    @FXML
    public void initialize() {
        // Load default view
        showUsers();
    }

    @FXML
    private void showUsers() {
        loadView("AdminUsersView");
        setActive(btnUsers);
    }

    @FXML
    private void showEvents() {
        loadView("AdminEventsView");
        setActive(btnEvents);
    }

    @FXML
    private void showStats() {
        loadView("AdminStatsView");
        setActive(btnStats);
    }

    @FXML
    private void handleLogout() {
        MainApp.setRoot("LoginView");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/" + fxml + ".fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button btn) {
        // Reset styles
        btnUsers.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
        btnEvents.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
        btnStats.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");

        // Set active style
        btn.setStyle(
                "-fx-background-color: #34495e; -fx-border-color: transparent transparent transparent #00e676; -fx-border-width: 0 0 0 5; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
    }
}
