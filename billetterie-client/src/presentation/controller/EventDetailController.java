package presentation.controller;

import app.MainApp;
import Model.Categorie;
import Model.Evenement;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventDetailController {

    @FXML
    private Label eventNameLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private VBox categoriesContainer;

    private Evenement currentEvent;

    public void setEvenement(Evenement event) {
        this.currentEvent = event;
        updateUI();
        loadCategories();
    }

    private void updateUI() {
        if (currentEvent != null) {
            eventNameLabel.setText(currentEvent.getNom());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm");
            dateLabel.setText(currentEvent.getDateEvenement().format(formatter));
            locationLabel.setText("Stade ID: " + currentEvent.getStadeId()); // Idealement recup le nom du stade
        }
    }

    private void loadCategories() {
        categoriesContainer.getChildren().clear();
        categoriesContainer.getChildren().add(new Label("Chargement des catégories..."));

        Task<List<Categorie>> task = new Task<>() {
            @Override
            protected List<Categorie> call() throws Exception {
                return MainApp.getService().getCategoriesDisponibles(currentEvent.getId());
            }
        };

        task.setOnSucceeded(e -> {
            List<Categorie> categories = task.getValue();
            categoriesContainer.getChildren().clear();

            if (categories == null || categories.isEmpty()) {
                categoriesContainer.getChildren().add(new Label("Aucune catégorie disponible."));
            } else {
                for (Categorie cat : categories) {
                    categoriesContainer.getChildren().add(createCategoryRow(cat));
                }
            }
        });

        task.setOnFailed(e -> {
            categoriesContainer.getChildren().clear();
            categoriesContainer.getChildren().add(new Label("Erreur de chargement."));
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private HBox createCategoryRow(Categorie cat) {
        HBox row = new HBox(20);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(15));

        VBox info = new VBox(5);
        Label name = new Label(cat.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label price = new Label(cat.getPrix() + " DH");
        price.setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;");
        Label stock = new Label(cat.getPlacesDisponibles() + " places restantes");
        stock.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        info.getChildren().addAll(name, price, stock);
        HBox.setHgrow(info, Priority.ALWAYS);

        Spinner<Integer> quantitySpinner = new Spinner<>();
        // Max quantity limited by availability (capped at 10 for safety)
        int max = Math.min(10, cat.getPlacesDisponibles());
        if (max < 1)
            max = 0; // Sold out

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0);
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setPrefWidth(80);
        if (max == 0)
            quantitySpinner.setDisable(true);

        Button addBtn = new Button("Ajouter");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setDisable(max == 0);

        addBtn.setOnAction(e -> {
            int qty = quantitySpinner.getValue();
            if (qty > 0) {
                addToCart(cat, qty);
            }
        });

        row.getChildren().addAll(info, quantitySpinner, addBtn);
        return row;
    }

    private void addToCart(Categorie cat, int quantity) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Assuming we simulate a session userID = 1 for now or get it from MainApp
                // context
                // But wait, the RMI service might need a logged in user.
                // For now, we'll try to use a static user if possible or just log success.
                // The current Service logic: reserverPlaces(userId, catId, qty)
                // We need the ID of the logged user.

                if (MainApp.getCurrentUser() == null)
                    throw new Exception("User not logged in");
                long userId = MainApp.getCurrentUser().getId();
                MainApp.getService().reserverPlaces(userId, cat.getId(), quantity);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Consulter le panier pour payer");
                alert.showAndWait();
            });
            // System.out.println("Réservation ajoutée au panier !");
        });

        task.setOnFailed(e -> {
            System.err.println("Erreur reservation");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleBack() {
        ClientShellController.getInstance().showEvents();
    }

    @FXML
    private void goToCart() {
        ClientShellController.getInstance().showCart();
    }
}
