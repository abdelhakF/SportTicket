package presentation.controller;

import app.MainApp;
import Model.Billet;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class MyTicketsController {

    @FXML
    private javafx.scene.layout.FlowPane ticketsContainer;

    @FXML
    public void initialize() {
        loadTickets();
    }

    @FXML
    private void handleRefresh() {
        loadTickets();
    }

    @FXML
    private void handleBack() {
        ClientShellController.getInstance().showEvents();
    }

    private void loadTickets() {
        ticketsContainer.getChildren().clear();
        ticketsContainer.getChildren().add(new Label("Chargement de vos billets..."));

        Task<List<Billet>> task = new Task<>() {
            @Override
            protected List<Billet> call() throws Exception {
                if (MainApp.getCurrentUser() == null)
                    return new java.util.ArrayList<>();
                long userId = MainApp.getCurrentUser().getId();
                return MainApp.getService().getBilletsClient(userId);
            }
        };

        task.setOnSucceeded(e -> {
            ticketsContainer.getChildren().clear();
            List<Billet> billets = task.getValue();

            if (billets == null || billets.isEmpty()) {
                ticketsContainer.getChildren().add(new Label("Vous n'avez aucun billet valide."));
            } else {
                for (Billet b : billets) {
                    ticketsContainer.getChildren().add(createTicketCard(b));
                }
            }
        });

        task.setOnFailed(e -> {
            ticketsContainer.getChildren().clear();
            Label err = new Label("Erreur de récupération.");
            err.setTextFill(Color.RED);
            ticketsContainer.getChildren().add(err);
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private VBox createTicketCard(Billet b) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setPrefHeight(400); // Taller for QR code content
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-border-color: #1a237e; -fx-border-width: 2px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: white;");

        // Header
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #1a237e; -fx-background-radius: 5 5 0 0;");
        header.setPadding(new javafx.geometry.Insets(15));

        Label brand = new Label("SPORTTICKET PRO");
        brand.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        header.getChildren().add(brand);

        // Info
        VBox info = new VBox(10);
        info.setPadding(new javafx.geometry.Insets(15));
        info.setAlignment(Pos.CENTER_LEFT);

        String nomCategorie = (b.getNomCategorie() != null && !b.getNomCategorie().trim().isEmpty()) 
                ? b.getNomCategorie() : "Catégorie inconnue";
        Label categoryLabel = new Label("CATÉGORIE: " + nomCategorie);
        categoryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Label refLabel = new Label("Ref: " + (b.getNumeroBillet() != null ? b.getNumeroBillet() : "N/A"));
        refLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-family: 'Monospaced';");

        String dateStr = (b.getDateEmission() != null) 
                ? b.getDateEmission().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A";
        Label dateLabel = new Label("Date d'achat: " + dateStr);

        info.getChildren().addAll(categoryLabel, refLabel, dateLabel);

        // QR Code Simulation
        VBox qrBox = new VBox();
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setPadding(new javafx.geometry.Insets(10));

        // Simulate QR with a black rectangle
        Rectangle qrSim = new Rectangle(120, 120, Color.BLACK);
        String qrCode = (b.getCodeQr() != null) ? b.getCodeQr() : "N/A";
        Label qrText = new Label(qrCode);
        qrText.setStyle("-fx-font-size: 9px; -fx-text-fill: grey;");

        qrBox.getChildren().addAll(qrSim, qrText);

        card.getChildren().addAll(header, info, qrBox);
        return card;
    }
}
