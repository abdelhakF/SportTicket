package presentation.controller;

import app.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ClientShellController {

    private static ClientShellController instance;

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnEvents;
    @FXML
    private Button btnTickets;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnCart;

    public ClientShellController() {
        instance = this;
    }

    public static ClientShellController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        showHome();
    }

    @FXML
    public void showEvents() {
        loadView("EventsListView");
        setActive(btnEvents);
    }

    @FXML
    public void showHome() {
        loadView("ClientHomeView");
        setActive(btnHome);
    }

    @FXML
    public void showTickets() {
        loadView("MyTicketsView");
        setActive(btnTickets);
    }

    @FXML
    public void showProfile() {
        loadView("ProfileView");
        setActive(btnProfile);
    }

    @FXML
    public void showCart() {
        loadView("CartView");
        setActive(btnCart);
    }

    @FXML
    private void handleLogout() {
        MainApp.setRoot("LoginView");
    }

    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/" + fxml + ".fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper to load view AND get controller (for passing data like Event details)
    public <T> T loadViewWithController(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/" + fxml + ".fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setActive(Button btn) {
        if (btn == null)
            return; // For programmatically loaded views like EventDetails

        btnHome.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
        btnEvents.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
        btnTickets.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
        btnProfile.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
        btnCart.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");

        btn.setStyle(
                "-fx-background-color: #283593; -fx-border-color: transparent transparent transparent #00e676; -fx-border-width: 0 0 0 5; -fx-font-size: 14px; -fx-padding: 15; -fx-cursor: hand; -fx-text-fill: white;");
    }
}
