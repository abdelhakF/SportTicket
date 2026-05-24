// impl/EvenementDaoImpl.java
package impl;

import dao.DBConnection;
import dao.EvenementDAO;
import Model.Evenement;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvenementDaoImpl implements EvenementDAO {

    // Constructeur vide (plus de connexion stockée ici)
    public EvenementDaoImpl() {
    }

    @Override
    public List<Evenement> findAVenir() {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE date_evenement > NOW() AND statut = 'A_VENIR' ORDER BY date_evenement";

        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Evenement> findAll() {
        List<Evenement> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM evenement ORDER BY date_evenement DESC")) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Evenement findById(long id) {
        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM evenement WHERE id = ?")) {
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

    @Override
    public Evenement save(Evenement e) {
        String sql = e.getId() == 0
                ? "INSERT INTO evenement(nom, date_evenement, sport_id, stade_id) VALUES(?, ?, ?, ?) RETURNING id"
                : "UPDATE evenement SET nom=?, date_evenement=?, sport_id=?, stade_id=? WHERE id=?";

        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, e.getNom());
                ps.setTimestamp(2, Timestamp.from(e.getDateEvenement().toInstant()));
                ps.setLong(3, e.getSportId());
                ps.setLong(4, e.getStadeId());
                if (e.getId() != 0) ps.setLong(5, e.getId());

                // Utilisation de executeQuery pour récupérer le RETURNING id
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) e.setId(rs.getLong(1));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return e;
    }

    @Override
    public boolean updateStatut(long id, String statut) {
        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (PreparedStatement ps = conn.prepareStatement("UPDATE evenement SET statut = ? WHERE id = ?")) {
                ps.setString(1, statut);
                ps.setLong(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(long id) {
        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM evenement WHERE id = ?")) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Evenement> search(String keyword) {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE nom ILIKE ? OR statut ILIKE ?";
        try {
            Connection conn = DBConnection.getConnection(); // Récupération fraîche
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String like = "%" + keyword + "%";
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

    private Evenement map(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setId(rs.getLong("id"));
        e.setNom(rs.getString("nom"));
        Timestamp ts = rs.getTimestamp("date_evenement");
        if (ts != null) e.setDateEvenement(ZonedDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()));
        e.setSportId(rs.getLong("sport_id"));
        e.setStadeId(rs.getLong("stade_id"));
        e.setStatut(rs.getString("statut"));
        return e;
    }
}