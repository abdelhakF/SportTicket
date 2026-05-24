package presentation.controller;

import app.MainApp;
import Model.Utilisateur;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class AdminUsersController {

    @FXML
    private TextField searchUserField;
    @FXML
    private TableView<Utilisateur> usersTable;
    @FXML
    private TableColumn<Utilisateur, Long> colUserId;
    @FXML
    private TableColumn<Utilisateur, String> colUserName;
    @FXML
    private TableColumn<Utilisateur, String> colUserEmail;
    @FXML
    private TableColumn<Utilisateur, String> colUserStatus;
    @FXML
    private TableColumn<Utilisateur, Void> colUserAction;

    @FXML
    public void initialize() {
        setupUserColumns();
        loadUsers("");
    }

    private void setupUserColumns() {
        colUserId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colUserName.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getNom() + " " + data.getValue().getPrenom()));
        colUserEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colUserStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));

        colUserAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    boolean isBlocked = "BLOQUE".equals(u.getStatut());
                    btn.setText(isBlocked ? "Débloquer" : "Bloquer");
                    btn.setStyle(isBlocked ? "-fx-background-color: #00e676;"
                            : "-fx-background-color: #e74c3c; -fx-text-fill: white;");

                    btn.setOnAction(event -> toggleBlockUser(u, !isBlocked));
                    setGraphic(btn);
                }
            }
        });
    }

    private void loadUsers(String query) {
        Task<List<Utilisateur>> task = new Task<>() {
            @Override
            protected List<Utilisateur> call() throws Exception {
                return MainApp.getService().listerUtilisateurs(query);
            }
        };
        task.setOnSucceeded(e -> usersTable.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Erreur chargement utilisateurs: " + task.getException().getMessage());
            a.show();
        });
        new Thread(task).start();
    }

    @FXML
    private void handleSearchUsers() {
        loadUsers(searchUserField.getText());
    }

    private void toggleBlockUser(Utilisateur u, boolean block) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                MainApp.getService().bloquerUtilisateur(u.getId(), block);
                return null;
            }
        };
        task.setOnSucceeded(e -> loadUsers(searchUserField.getText()));
        task.setOnFailed(e -> {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Erreur mise à jour statut: " + task.getException().getMessage());
            a.show();
        });
        new Thread(task).start();
    }
}
