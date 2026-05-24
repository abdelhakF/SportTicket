package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.TicketService;
import java.rmi.Naming;

public class MainApp extends Application {

    private static TicketService service;
    private static Stage primaryStage;

    // Simple Session Management
    private static Model.Utilisateur currentUser;

    public static void setCurrentUser(Model.Utilisateur user) {
        currentUser = user;
    }

    public static Model.Utilisateur getCurrentUser() {
        return currentUser;
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("DEBUG: Starting application...");
        primaryStage = stage;

        // Load Home View
        try {
            System.out.println("DEBUG: Loading HomeView.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/HomeView.fxml"));
            Parent root = loader.load();
            System.out.println("DEBUG: HomeView loaded.");

            Scene scene = new Scene(root);
            System.out.println("DEBUG: Loading CSS...");
            scene.getStylesheets().add(getClass().getResource("/resources/css/bootstrap.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
            System.out.println("DEBUG: CSS loaded.");

            stage.setTitle("SportTicket Pro");
            stage.setScene(scene);
            stage.setMaximized(true);

            System.out.println("DEBUG: Showing stage...");
            stage.show();
            System.out.println("DEBUG: Stage shown.");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during startup:");
            e.printStackTrace();
        }

        // Connect to server in background to avoid blocking UI
        new Thread(() -> {
            System.out.println("DEBUG: Connecting to server...");
            connectToServer();
        }).start();
    }

    private void connectToServer() {
        try {
            service = (TicketService) Naming.lookup("rmi://localhost:1099/TicketService");
            System.out.println("Connecté au serveur RMI.");
        } catch (Exception e) {
            System.err.println("Impossible de se connecter au serveur : " + e.getMessage());
            // TODO: Afficher une alerte graphique ici
        }
    }

    public static TicketService getService() {
        return service;
    }

    public static void setRoot(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/presentation/view/" + fxml + ".fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MainApp.class.getResource("/resources/css/bootstrap.css").toExternalForm());
            scene.getStylesheets().add(MainApp.class.getResource("/resources/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
