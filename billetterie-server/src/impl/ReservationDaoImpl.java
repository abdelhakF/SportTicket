package impl;

import dao.DBConnection;
import dao.ReservationDAO;
import Model.Reservation;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDaoImpl implements ReservationDAO {

    public ReservationDaoImpl() {
    }

    @Override
    public List<Reservation> findEnAttenteByUtilisateur(long utilisateurId) {
        List<Reservation> list = new ArrayList<>();

        // REQUÊTE AVEC JOIN + FILTRE SUR L'HEURE LOCALE
        String sql = """
            SELECT r.*, c.nom as cat_nom, e.nom as evt_nom
            FROM reservation r
            JOIN categorie c ON r.categorie_id = c.id
            JOIN evenement e ON c.evenement_id = e.id
            WHERE r.utilisateur_id = ? 
            AND r.statut = 'EN_ATTENTE' 
            AND r.date_expiration > ? 
        """;

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, utilisateurId);
                // On passe l'heure locale de votre PC
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Reservation r = map(rs);
                        // On remplit les noms
                        r.setNomCategorie(rs.getString("cat_nom"));
                        r.setNomEvenement(rs.getString("evt_nom"));
                        list.add(r);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Reservation> findExpirees() {
        List<Reservation> list = new ArrayList<>();
        // On cherche les réservations 'EN_ATTENTE' dont l'heure limite est passée
        String sql = "SELECT * FROM reservation WHERE statut='EN_ATTENTE' AND date_expiration < ?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Reservation save(Reservation r) {
        // Insertion SANS prix_total
        String sql = "INSERT INTO reservation(utilisateur_id, categorie_id, quantite, prix_unitaire, date_expiration, statut, date_creation) VALUES(?,?,?,?,?,?,?) RETURNING id";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, r.getUtilisateurId());
                ps.setLong(2, r.getCategorieId());
                ps.setInt(3, r.getQuantite());
                ps.setBigDecimal(4, r.getPrixUnitaire());

                // On enregistre l'heure telle quelle (sans conversion UTC)
                ps.setTimestamp(5, Timestamp.valueOf(r.getDateExpiration().toLocalDateTime()));
                ps.setString(6, "EN_ATTENTE");
                ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) r.setId(rs.getLong(1));
                }

                // Calcul du prix total côté Java
                if (r.getPrixTotal() == null && r.getPrixUnitaire() != null) {
                    r.setPrixTotal(r.getPrixUnitaire().multiply(BigDecimal.valueOf(r.getQuantite())));
                }

                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Autres méthodes standard ---
    @Override
    public Reservation findById(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM reservation WHERE id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return map(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean updateStatut(long id, String statut) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE reservation SET statut=? WHERE id=?")) {
                ps.setString(1, statut);
                ps.setLong(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { return false; }
    }

    @Override public List<Reservation> findAllEnAttente() { return new ArrayList<>(); }
    @Override public boolean delete(long id) { return false; }
    @Override public int nettoyerReservationsExpirees() { return 0; }
    @Override public List<Reservation> findConfirmeesByUtilisateur(long utilisateurId) { return new ArrayList<>(); }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getLong("id"));
        r.setUtilisateurId(rs.getLong("utilisateur_id"));
        r.setCategorieId(rs.getLong("categorie_id"));
        r.setQuantite(rs.getInt("quantite"));
        r.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));

        if (r.getPrixUnitaire() != null) {
            r.setPrixTotal(r.getPrixUnitaire().multiply(BigDecimal.valueOf(r.getQuantite())));
        }

        Timestamp c = rs.getTimestamp("date_creation");
        if (c != null) r.setDateCreation(c.toLocalDateTime().atZone(ZoneId.systemDefault()));

        Timestamp e = rs.getTimestamp("date_expiration");
        if (e != null) r.setDateExpiration(e.toLocalDateTime().atZone(ZoneId.systemDefault()));

        r.setStatut(rs.getString("statut"));
        return r;
    }
}