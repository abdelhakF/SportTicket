package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/Billetriedb";
    private static final String USER = "postgres";  // ton utilisateur PostgreSQL
    private static final String PASSWORD = "root";  // ton mot de passe PostgreSQL

    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Charger le driver PostgreSQL
                Class.forName("org.postgresql.Driver");
                // Créer la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base PostgreSQL réussie !");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver PostgreSQL non trouvé !");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion à la base fermée.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
