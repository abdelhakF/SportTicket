// impl/StadeDaoImpl.java
package impl;

import dao.DBConnection;
import dao.StadeDAO;
import Model.Stade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StadeDaoImpl implements StadeDAO {

    public StadeDaoImpl() {
        // Constructeur vide - connexion récupérée à chaque méthode
    }

    @Override public List<Stade> findAll() {
        List<Stade> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement st = conn.createStatement(); 
                 ResultSet rs = st.executeQuery("SELECT * FROM stade ORDER BY nom")) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    @Override public Stade findById(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM stade WHERE id=?")) {
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

    @Override public Stade findByNomAndVille(String nom, String ville) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM stade WHERE LOWER(nom)=LOWER(?) AND LOWER(ville)=LOWER(?)")) {
                ps.setString(1, nom.trim());
                ps.setString(2, ville.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return map(rs);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    @Override public List<Stade> search(String keyword) {
        List<Stade> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return findAll();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM stade WHERE nom ILIKE ? OR ville ILIKE ? ORDER BY nom")) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like); 
                ps.setString(2, like);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    @Override public Stade save(Stade s) {
        if (s.getId() == 0) {
            // INSERT avec RETURNING
            String sql = "INSERT INTO stade(nom, ville, capacite) VALUES(?,?,?) RETURNING id";
            try {
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, s.getNom());
                    ps.setString(2, s.getVille());
                    ps.setInt(3, s.getCapacite());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            s.setId(rs.getLong(1));
                            return s;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'insertion du stade '" + s.getNom() + "' à " + s.getVille() + ": " + e.getMessage());
                e.printStackTrace();
                // Si c'est une violation de contrainte UNIQUE, on essaie de récupérer le stade existant
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // 23xxx = constraint violation
                    return findByNomAndVille(s.getNom(), s.getVille());
                }
                return null;
            }
        } else {
            // UPDATE
            String sql = "UPDATE stade SET nom=?, ville=?, capacite=? WHERE id=?";
            try {
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, s.getNom());
                    ps.setString(2, s.getVille());
                    ps.setInt(3, s.getCapacite());
                    ps.setLong(4, s.getId());
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        return s;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la mise à jour du stade '" + s.getNom() + "': " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override public boolean delete(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM stade WHERE id=?")) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { 
            return false; 
        }
    }

    private Stade map(ResultSet rs) throws SQLException {
        Stade s = new Stade();
        s.setId(rs.getLong("id"));
        s.setNom(rs.getString("nom"));
        s.setVille(rs.getString("ville"));
        s.setCapacite(rs.getInt("capacite"));
        return s;
    }
}