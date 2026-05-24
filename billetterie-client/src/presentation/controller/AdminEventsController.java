package presentation.controller;

import app.MainApp;
import Model.Evenement;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminEventsController {

    @FXML
    private TextField evtNameField;
    @FXML
    private DatePicker evtDatePicker;
    @FXML
    private TextField evtTimeField;
    @FXML
    private ComboBox<String> evtSportCombo;
    @FXML
    private TextField evtStadiumField;
    @FXML
    private TextField evtCityField;
    @FXML
    private TextField evtCapacityField;
    @FXML
    private TableView<Evenement> eventsTable;
    @FXML
    private TableColumn<Evenement, Long> colEvtId;
    @FXML
    private TableColumn<Evenement, String> colEvtName;
    @FXML
    private TableColumn<Evenement, String> colEvtDate;

    @FXML
    public void initialize() {
        setupEventColumns();
        loadEvents();
        loadSports();
    }

    private void loadSports() {
        Task<List<Model.Sport>> task = new Task<>() {
            @Override
            protected List<Model.Sport> call() throws Exception {
                return MainApp.getService().rechercherSports("");
            }
        };
        task.setOnSucceeded(e -> {
            List<String> sportNames = task.getValue().stream().map(Model.Sport::getNom).toList();
            evtSportCombo.setItems(FXCollections.observableArrayList(sportNames));
        });
        new Thread(task).start();
    }

    private void setupEventColumns() {
        colEvtId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colEvtName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colEvtDate
                .setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateEvenement().format(fmt)));
    }

    private void loadEvents() {
        Task<List<Evenement>> task = new Task<>() {
            @Override
            protected List<Evenement> call() throws Exception {
                return MainApp.getService().getEvenementsAVenir();
            }
        };
        task.setOnSucceeded(e -> eventsTable.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    @FXML
    private void handleCreateEvent() {
        // Validation des champs
        String name = evtNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Le nom de l'événement est requis");
            return;
        }

        if (evtDatePicker.getValue() == null) {
            showError("La date est requise");
            return;
        }

        String timeStr = evtTimeField.getText().trim();
        if (timeStr.isEmpty()) {
            timeStr = "20:00"; // Valeur par défaut
        } else {
            // Validation du format d'heure (HH:mm)
            if (!timeStr.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showError("Format d'heure invalide. Utilisez HH:mm (ex: 20:00)");
                return;
            }
        }

        String sport = evtSportCombo.getValue() != null ? evtSportCombo.getValue()
                : evtSportCombo.getEditor().getText();
        if (sport == null || sport.trim().isEmpty()) {
            showError("Le sport est requis");
            return;
        }

        String stadium = evtStadiumField.getText().trim();
        if (stadium.isEmpty()) {
            showError("Le nom du stade est requis");
            return;
        }

        String city = evtCityField.getText().trim();
        if (city.isEmpty()) {
            showError("La ville est requise");
            return;
        }

        int capacity = 0;
        try {
            capacity = Integer.parseInt(evtCapacityField.getText().trim());
            if (capacity <= 0) {
                showError("La capacité doit être supérieure à 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("La capacité doit être un nombre valide");
            return;
        }

        // Formatage de la date au format yyyy-MM-dd HH:mm
        String formattedDate = evtDatePicker.getValue().toString() + " " + timeStr;
        int finalCapacity = capacity;

        // Désactiver le bouton pendant la création
        Task<Evenement> task = new Task<>() {
            @Override
            protected Evenement call() throws Exception {
                return MainApp.getService().creerEvenementComplet(name, formattedDate, sport.trim(), stadium, city,
                        finalCapacity);
            }
        };

        task.setOnSucceeded(e -> {
            Evenement created = task.getValue();
            if (created != null) {
                Alert a = new Alert(Alert.AlertType.INFORMATION,
                        "Événement créé avec succès !\nID: " + created.getId());
                a.setHeaderText("Succès");
                a.show();
                // Réinitialiser le formulaire
                clearForm();
                loadEvents();
            }
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            String errorMessage = "Erreur lors de la création";
            if (exception != null && exception.getCause() != null) {
                errorMessage = exception.getCause().getMessage();
            } else if (exception != null) {
                errorMessage = exception.getMessage();
            }
            showError(errorMessage);
            if (exception != null) {
                exception.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    @FXML
    private void handleDeleteEvent() {
        Evenement selected = eventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner un événement à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'événement " + selected.getNom() + " ?");
        alert.setContentText("Cette action est irréversible. Les réservations associées seront perdues.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return MainApp.getService().deleteEvenement(selected.getId());
                }
            };

            task.setOnSucceeded(e -> {
                if (task.getValue()) {
                    loadEvents(); // Refresh list
                    Alert succ = new Alert(Alert.AlertType.INFORMATION, "Événement supprimé avec succès.");
                    succ.show();
                } else {
                    showError("Impossible de supprimer l'événement.");
                }
            });

            task.setOnFailed(e -> {
                showError("Erreur lors de la suppression : " + task.getException().getMessage());
            });

            new Thread(task).start();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Erreur");
        alert.show();
    }

    private void clearForm() {
        evtNameField.clear();
        evtDatePicker.setValue(null);
        evtTimeField.clear();
        evtSportCombo.setValue(null);
        evtStadiumField.clear();
        evtCityField.clear();
        evtCapacityField.clear();
    }
}
