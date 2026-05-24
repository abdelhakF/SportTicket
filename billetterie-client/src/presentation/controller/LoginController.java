package presentation.controller;

import app.MainApp;
import Model.Utilisateur;
import service.TicketService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleBack() {
        MainApp.setRoot("HomeView");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        statusLabel.setVisible(false);

        // RMI Call in Background Support
        Task<Utilisateur> loginTask = new Task<>() {
            @Override
            protected Utilisateur call() throws Exception {
                TicketService service = MainApp.getService();
                if (service == null) {
                    throw new Exception("Service non disponible. Vérifiez la connexion au serveur.");
                }
                return service.connexion(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            Utilisateur user = loginTask.getValue();
            if (user != null) {
                // TODO: Store user in MainApp or Session context
                MainApp.setCurrentUser(user);
                System.out.println("Login Successful for: " + user.getNom());

                if ("ADMIN".equals(user.getRole())) {
                    MainApp.setRoot("AdminShellView"); // Admin Sidebar Layout
                } else {
                    MainApp.setRoot("ClientShellView"); // Client Sidebar Layout
                }
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            ex.printStackTrace();
            showError("Erreur de connexion au serveur.");
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void handleRegister() {
        MainApp.setRoot("RegisterView");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }
}
