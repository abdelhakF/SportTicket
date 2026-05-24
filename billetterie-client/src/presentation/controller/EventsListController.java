package presentation.controller;

import app.MainApp;
import Model.Evenement;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsListController {

    @FXML
    private FlowPane eventsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private javafx.scene.control.ComboBox<String> sportCombo;

    @FXML
    public void initialize() {
        loadSports();
        loadEvents("", null);

        sportCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadEvents(searchField.getText(), newVal);
        });
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
            javafx.collections.ObservableList<String> items = javafx.collections.FXCollections
                    .observableArrayList(sportNames);
            items.add(0, "Tous les sports"); // Option pour tout afficher
            sportCombo.setItems(items);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleSearch() {
        loadEvents(searchField.getText(), sportCombo.getValue());
    }

    @FXML
    private void handleLogout() {
        // Clear session logic here
        MainApp.setRoot("LoginView");
    }

    private void loadEvents(String query, String sport) {
        eventsContainer.getChildren().clear();
        eventsContainer.getChildren().add(new Label("Chargement des événements..."));

        Task<List<Evenement>> task = new Task<>() {
            @Override
            protected List<Evenement> call() throws Exception {
                List<Evenement> events;
                // Si une requête texte est présente, on cherche par texte
                if (query != null && !query.trim().isEmpty()) {
                    events = MainApp.getService().rechercherEvenements(query);
                } else {
                    // Sinon on récupère tous les événements (historique + futur pour répondre à la
                    // demande "tous les événements")
                    events = MainApp.getService().rechercherEvenements("");
                }

                // Filtrage côté client pour le sport (plus simple que de modifier toutes les
                // requêtes SQL pour l'instant)
                if (sport != null && !sport.isEmpty() && !"Tous les sports".equals(sport)) {
                    // Note: C'est une simplification. Idéalement on devrait faire le filtre côté
                    // serveur ou avoir l'objet Sport complet dans l'événement
                    // Ici on va devoir faire un tour de passe-passe car Event ne contient que
                    // sportId
                    // Pour l'instant on va recharger les details ou supporter le filtrage basique
                    // si on avait le nom
                    // Comme on a que l'ID, on va devoir récupérer les sports pour mapper le nom au
                    // ID
                    return events; // Placeholder, see logic below in setOnSucceeded
                }
                return events;
            }
        };

        task.setOnSucceeded(e -> {
            eventsContainer.getChildren().clear();
            List<Evenement> events = task.getValue();

            // FILTRAGE SUPPLÉMENTAIRE CÔTÉ CLIENT (car on manque de jointure simple ici
            // sans modifier tout le modèle)
            // On va filtrer ceux qui ne correspondent pas au sport sélectionné
            // Attention: ev.getSportId() est un long, sport est un String.
            // On a besoin de mapper le String sport à un ID.

            if (sport != null && !sport.isEmpty() && !"Tous les sports".equals(sport)) {
                // On doit faire un filtrage asynchrone un peu moche ou alors supposons qu'on
                // fait confiance à la recherche
                // Pour faire propre rapidement : on va récupérer la liste des sports et trouver
                // l'id correspondant
                filterBySportId(events, sport);
            } else {
                displayEvents(events);
            }
        });

        task.setOnFailed(e -> {
            eventsContainer.getChildren().clear();
            eventsContainer.getChildren().add(new Label("Erreur lors du chargement des données."));
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void filterBySportId(List<Evenement> events, String sportName) {
        Task<List<Model.Sport>> sportTask = new Task<>() {
            @Override
            protected List<Model.Sport> call() throws Exception {
                return MainApp.getService().rechercherSports(sportName);
            }
        };
        sportTask.setOnSucceeded(e -> {
            List<Model.Sport> sports = sportTask.getValue();
            // On cherche le sport exact
            long sportId = -1;
            for (Model.Sport s : sports) {
                if (s.getNom().equalsIgnoreCase(sportName)) {
                    sportId = s.getId();
                    break;
                }
            }

            final long finalId = sportId;
            List<Evenement> filtered = events.stream()
                    .filter(evt -> evt.getSportId() == finalId)
                    .toList();
            displayEvents(filtered);
        });
        new Thread(sportTask).start();
    }

    private void displayEvents(List<Evenement> events) {
        if (events == null || events.isEmpty()) {
            eventsContainer.getChildren().add(new Label("Aucun événement trouvé."));
        } else {
            for (Evenement evt : events) {
                eventsContainer.getChildren().add(createEventCard(evt));
            }
        }
    }

    private VBox createEventCard(Evenement evt) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        card.setPrefHeight(200);
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(evt.getNom());
        title.getStyleClass().add("label-title");
        title.setWrapText(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label date = new Label(evt.getDateEvenement().format(formatter));
        date.setStyle("-fx-text-fill: #7f8c8d;");

        Label stadium = new Label("📍 " + evt.getStadeId()); // Note: Need to fetch Stade name in real app or update
                                                             // Model toString
        stadium.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

        // Spacer
        VBox spacer = new VBox();
        spacer.setPrefHeight(20);

        Button bookBtn = new Button("RÉSERVER");
        bookBtn.getStyleClass().add("button-action");
        bookBtn.setMaxWidth(Double.MAX_VALUE);

        bookBtn.setOnAction(e -> {
            EventDetailController controller = ClientShellController.getInstance()
                    .loadViewWithController("EventDetailView");
            if (controller != null) {
                controller.setEvenement(evt);
            }
        });

        card.getChildren().addAll(title, date, stadium, spacer, bookBtn);
        return card;
    }
}
