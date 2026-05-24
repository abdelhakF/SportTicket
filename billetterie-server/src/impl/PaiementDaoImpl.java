// impl/PaiementDaoImpl.java
package impl;

import dao.DBConnection;
import dao.PaiementDAO;
import Model.Paiement;
import java.math.BigDecimal;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaiementDaoImpl implements PaiementDAO {

    // SUPPRESSION de la connexion stockée en attribut
    // SUPPRESSION du throws SQLException dans le constructeur
    public PaiementDaoImpl() {
    }

    @Override
    public Paiement save(Paiement p) {
        // Insertion avec retour de l'ID généré
        String sql = "INSERT INTO paiement(reservation_id, utilisateur_id, montant, methode, reference) VALUES(?,?,?,?,?) RETURNING id";

        try {
            Connection conn = DBConnection.getConnection(); // Récupération connexion fraîche

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (p.getReservationId() != null) ps.setLong(1, p.getReservationId());
                else ps.setNull(1, Types.BIGINT);

                ps.setLong(2, p.getUtilisateurId());
                ps.setBigDecimal(3, p.getMontant());
                ps.setString(4, p.getMethode());
                ps.setString(5, p.getReference());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) p.setId(rs.getLong(1));
                }
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Paiement findById(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM paiement WHERE id=?")) {
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
    public List<Paiement> findByUtilisateurId(long utilisateurId) {
        List<Paiement> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM paiement WHERE utilisateur_id=? ORDER BY date_paiement DESC")) {
                ps.setLong(1, utilisateurId);
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
    public BigDecimal getChiffreAffairesTotal() {
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(montant),0) FROM paiement WHERE statut='PAYE'")) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Object[]> getCAParEvenement() {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT e.nom, sp.nom, st.nom, COALESCE(SUM(p.montant),0)
            FROM paiement p
            JOIN reservation r ON p.reservation_id = r.id
            JOIN categorie c ON r.categorie_id = c.id
            JOIN evenement e ON c.evenement_id = e.id
            JOIN sport sp ON e.sport_id = sp.id
            JOIN stade st ON e.stade_id = st.id
            WHERE p.statut='PAYE'
            GROUP BY e.id, e.nom, sp.nom, st.nom
            ORDER BY SUM(p.montant) DESC
            """;

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getBigDecimal(4)
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Paiement map(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setId(rs.getLong("id"));
        long resId = rs.getLong("reservation_id");
        p.setReservationId(rs.wasNull() ? null : resId);
        p.setUtilisateurId(rs.getLong("utilisateur_id"));
        p.setMontant(rs.getBigDecimal("montant"));
        p.setMethode(rs.getString("methode"));
        p.setStatut(rs.getString("statut"));
        p.setReference(rs.getString("reference"));

        Timestamp ts = rs.getTimestamp("date_paiement");
        if (ts != null) p.setDatePaiement(ZonedDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        return p;
    }
}