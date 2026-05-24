package presentation.controller;

import app.MainApp;
import Model.Reservation;
import service.TicketService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.util.List;

public class CartController {

    @FXML
    private TableView<Reservation> cartTable;
    @FXML
    private TableColumn<Reservation, String> colEvent;
    @FXML
    private TableColumn<Reservation, String> colCategory;
    @FXML
    private TableColumn<Reservation, Double> colPrice;
    @FXML
    private TableColumn<Reservation, Integer> colQty;
    @FXML
    private TableColumn<Reservation, Double> colTotal;
    @FXML
    private TableColumn<Reservation, Void> colActions;
    @FXML
    private Label totalLabel;

    @FXML
    public void initialize() {
        setupColumns();
        loadCart();
    }

    private void setupColumns() {
        // Note: Relation objects (Event/Category) might be null in Reservation if not
        // eagerly fetched.
        // Assuming Reservation object has IDs, we might need to fetch details or assume
        // Server sends enriched objects.
        // For this demo, we'll display IDs if names aren't available, or simple
        // placeholders.

        colEvent.setCellValueFactory(
                cellData -> {
                    String nomEvent = cellData.getValue().getNomEvenement();
                    return new SimpleStringProperty(nomEvent != null && !nomEvent.trim().isEmpty() 
                            ? nomEvent : "Événement inconnu");
                });

        colCategory.setCellValueFactory(
                cellData -> {
                    String nomCat = cellData.getValue().getNomCategorie();
                    return new SimpleStringProperty(nomCat != null && !nomCat.trim().isEmpty() 
                            ? nomCat : "Catégorie inconnue");
                });

        colPrice.setCellValueFactory(
                cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaire().doubleValue()));
        colQty.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantite()));

        colTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getPrixUnitaire().doubleValue() * cellData.getValue().getQuantite();
            return new SimpleObjectProperty<>(total);
        });

        // Add "Delete" button column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("X");
            {
                btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Reservation data = getTableView().getItems().get(getIndex());
                    handleDelete(data);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void loadCart() {
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() throws Exception {
                // Fetch reservations with logic "EN_ATTENTE"
                if (MainApp.getCurrentUser() == null)
                    return new java.util.ArrayList<>();
                TicketService service = MainApp.getService();
                if (service == null) {
                    throw new Exception("Service non disponible. Vérifiez la connexion au serveur.");
                }
                long userId = MainApp.getCurrentUser().getId();
                return service.getReservationsEnAttente(userId);
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<Reservation> data = FXCollections.observableArrayList(task.getValue());
            cartTable.setItems(data);
            updateTotal(data);
        });

        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void updateTotal(List<Reservation> items) {
        double total = items.stream()
                .mapToDouble(r -> r.getPrixUnitaire().doubleValue() * r.getQuantite())
                .sum();
        totalLabel.setText(String.format("%.2f DH", total));
    }

    private void handleDelete(Reservation r) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                TicketService service = MainApp.getService();
                if (service == null) {
                    throw new Exception("Service non disponible.");
                }
                service.annulerReservation(r.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> loadCart()); // Refresh
        new Thread(task).start();
    }

    @FXML
    private void handlePay() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate payment for each item in cart logic
                TicketService service = MainApp.getService();
                if (service == null) {
                    throw new Exception("Service non disponible.");
                }
                List<Reservation> cart = cartTable.getItems();
                for (Reservation r : cart) {
                    service.payerReservation(r.getId(), "CARTE_BANCAIRE");
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paiement réussi");
            alert.setHeaderText(null);
            alert.setContentText("Votre paiement a été accepté. Vos billets sont disponibles.");
            alert.showAndWait();
            loadCart(); // Should be empty
            ClientShellController.getInstance().showTickets();
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Le paiement a échoué.");
            alert.show();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleClearParams() {
        // Logic to remove all
    }

    @FXML
    private void handleBack() {
        ClientShellController.getInstance().showEvents();
    }
}
