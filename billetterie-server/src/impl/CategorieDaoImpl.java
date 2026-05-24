// impl/CategorieDaoImpl.java
package impl;

import dao.CategorieDAO;
import dao.DBConnection;
import Model.Categorie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieDaoImpl implements CategorieDAO {

    // 1. SUPPRESSION de l'attribut 'private final Connection conn'
    // 2. SUPPRESSION du constructeur qui initialise la connexion.
    //    On récupère la connexion "fraîche" à chaque méthode.

    public CategorieDaoImpl() {
        // Constructeur vide explicite (optionnel, mais propre)
    }

    @Override
    public List<Categorie> findByEvenementId(long evenementId) {
        List<Categorie> categories = new ArrayList<>();
        String sql = """
                SELECT id, evenement_id, nom, prix, places_total,
                       places_reservees, places_vendues, places_disponibles
                FROM categorie
                WHERE evenement_id = ? AND places_disponibles > 0
                ORDER BY prix DESC
                """;

        // CORRECTION : Appel à DBConnection.getConnection() ici
        try {
            Connection conn = DBConnection.getConnection();
            // Note : on ne met pas 'conn' dans le try(...) pour ne pas la fermer
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, evenementId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) categories.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public Categorie findById(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM categorie WHERE id = ?")) {
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
    public Categorie save(Categorie c) {
        String sql = c.getId() == 0
                ? "INSERT INTO categorie(evenement_id, nom, prix, places_total) VALUES(?, ?, ?, ?) RETURNING id"
                : "UPDATE categorie SET evenement_id=?, nom=?, prix=?, places_total=? WHERE id=?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, c.getEvenementId());
                ps.setString(2, c.getNom());
                ps.setBigDecimal(3, c.getPrix());
                ps.setInt(4, c.getPlacesTotal());
                if (c.getId() != 0) ps.setLong(5, c.getId());

                // Utilisation de executeQuery car il y a un RETURNING (PostgreSQL spécifique)
                // ou pour un update standard qui ne retourne rien, executeUpdate est plus standard,
                // mais executeQuery fonctionne souvent si le driver est permissif ou s'il y a retour.
                if (c.getId() == 0) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) c.setId(rs.getLong(1));
                    }
                } else {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return c;
    }

    @Override
    public boolean updatePlaces(long id, int reserveesToAdd, int venduesToAdd) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE categorie SET places_reservees = places_reservees + ?, places_vendues = places_vendues + ? WHERE id = ?")) {
                ps.setInt(1, reserveesToAdd);
                ps.setInt(2, venduesToAdd);
                ps.setLong(3, id);
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
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM categorie WHERE id = ?")) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Categorie map(ResultSet rs) throws SQLException {
        Categorie c = new Categorie();
        c.setId(rs.getLong("id"));
        c.setEvenementId(rs.getLong("evenement_id"));
        c.setNom(rs.getString("nom"));
        c.setPrix(rs.getBigDecimal("prix"));
        c.setPlacesTotal(rs.getInt("places_total"));
        c.setPlacesReservees(rs.getInt("places_reservees"));
        c.setPlacesVendues(rs.getInt("places_vendues"));
        c.setPlacesDisponibles(rs.getInt("places_disponibles"));
        return c;
    }
}