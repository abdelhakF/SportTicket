package presentation.controller;

import app.MainApp;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import java.util.List;
import java.util.Map;

public class AdminStatsController {

    @FXML
    private BarChart<String, Number> statsApp;

    @FXML
    public void initialize() {
        refreshStats();
    }

    @FXML
    private void refreshStats() {
        statsApp.getData().clear();

        Task<List<Object[]>> task = new Task<>() {
            @Override
            protected List<Object[]> call() throws Exception {
                return MainApp.getService().getCAParEvenement();
            }
        };

        task.setOnSucceeded(e -> {
            List<Object[]> data = task.getValue();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Chiffre d'Affaires par Événement");

            if (data != null) {
                for (Object[] row : data) {
                    try {
                        String eventName = (String) row[0];
                        Number revenue = (Number) row[1]; // BigDecimal or Double
                        series.getData().add(new XYChart.Data<>(eventName, revenue));
                    } catch (Exception ex) {
                        System.err.println("Erreur parsing stats row: " + ex.getMessage());
                    }
                }
            }
            statsApp.getData().add(series);
        });

        task.setOnFailed(e -> {
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "Erreur chargement statistiques: " + task.getException().getMessage());
            a.show();
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }
}
