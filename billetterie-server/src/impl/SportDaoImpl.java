// impl/SportDaoImpl.java
package impl;

import dao.DBConnection;
import dao.SportDAO;
import Model.Sport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SportDaoImpl implements SportDAO {

    public SportDaoImpl() {
        // Constructeur vide - connexion récupérée à chaque méthode
    }

    @Override public List<Sport> findAll() {
        List<Sport> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement st = conn.createStatement(); 
                 ResultSet rs = st.executeQuery("SELECT * FROM sport ORDER BY nom")) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    @Override public Sport findById(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sport WHERE id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return map(rs);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    @Override public Sport findByNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return null;
        }
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sport WHERE LOWER(nom)=LOWER(?)")) {
                ps.setString(1, nom.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return map(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du sport '" + nom + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override public List<Sport> searchByNom(String keyword) {
        List<Sport> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return findAll();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * from sport WHERE nom ILIKE ? ORDER BY nom")) {
                ps.setString(1, "%" + keyword.trim() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    @Override public Sport save(Sport s) {
        if (s == null || s.getNom() == null || s.getNom().trim().isEmpty()) {
            System.err.println("Tentative de sauvegarde d'un sport invalide");
            return null;
        }
        
        if (s.getId() == 0) {
            // INSERT avec RETURNING
            String sql = "INSERT INTO sport(nom) VALUES(?) RETURNING id";
            try {
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, s.getNom().trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            s.setId(rs.getLong(1));
                            System.out.println("Sport créé avec succès: " + s.getNom() + " (ID: " + s.getId() + ")");
                            return s;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'insertion du sport '" + s.getNom() + "': " + e.getMessage());
                System.err.println("SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
                e.printStackTrace();
                // Si c'est une violation de contrainte UNIQUE, on essaie de récupérer le sport existant
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // 23xxx = constraint violation
                    System.out.println("Violation de contrainte UNIQUE détectée, recherche du sport existant...");
                    Sport existant = findByNom(s.getNom());
                    if (existant != null) {
                        System.out.println("Sport existant trouvé: " + existant.getNom() + " (ID: " + existant.getId() + ")");
                        return existant;
                    }
                }
                return null;
            }
        } else {
            // UPDATE
            String sql = "UPDATE sport SET nom=? WHERE id=?";
            try {
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, s.getNom().trim());
                    ps.setLong(2, s.getId());
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        return s;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la mise à jour du sport '" + s.getNom() + "': " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override public boolean delete(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sport WHERE id=?")) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { 
            return false; 
        }
    }

    private Sport map(ResultSet rs) throws SQLException {
        Sport s = new Sport();
        s.setId(rs.getLong("id"));
        s.setNom(rs.getString("nom"));
        return s;
    }
}