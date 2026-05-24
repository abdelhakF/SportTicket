package presentation.controller;

import app.MainApp;
import Model.Utilisateur;
import service.TicketService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class RegisterController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label statusLabel;

    @FXML
    private void handleBack() {
        MainApp.setRoot("HomeView");
    }

    @FXML
    private void handleRegister() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String pwd = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!pwd.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        statusLabel.setVisible(false);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                TicketService service = MainApp.getService();
                if (service == null) {
                    throw new Exception("Service non disponible. Vérifiez la connexion au serveur.");
                }
                Utilisateur u = new Utilisateur();
                u.setNom(nom);
                u.setPrenom(prenom);
                u.setEmail(email);
                u.setTelephone(phone);
                u.setMotDePasse(pwd); // Server should hash this
                u.setRole("CLIENT");

                service.inscription(u);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            // Success
            statusLabel.setText("Compte créé avec succès ! Redirection...");
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setVisible(true);

            // Delay redirect slightly or just go
            MainApp.setRoot("LoginView");
        });

        task.setOnFailed(e -> {
            showError("Erreur lors de l'inscription (Email déjà utilisé ?).");
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleLoginLink() {
        MainApp.setRoot("LoginView");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(true);
    }
}
