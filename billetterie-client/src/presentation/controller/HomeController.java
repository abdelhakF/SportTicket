package presentation.controller;

import app.MainApp;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void handleLogin() {
        MainApp.setRoot("LoginView");
    }

    @FXML
    private void handleRegister() {
        MainApp.setRoot("RegisterView");
    }

    @FXML
    private void handleBack() {
        MainApp.setRoot("HomeView");
    }

    @FXML
    private void goToEvents() {
        MainApp.setRoot("EventsListView");
    }

    @FXML
    private void showTickets() {
        MainApp.setRoot("GuestTicketsView");
    }

    @FXML
    private void showContact() {
        MainApp.setRoot("ContactView");
    }
}
