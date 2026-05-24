package impl;

import dao.DBConnection;
import dao.BilletDAO;
import Model.Billet;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class BilletDaoImpl implements BilletDAO {

    public BilletDaoImpl() {
    }

    @Override
    public List<Billet> findByUtilisateurId(long utilisateurId) {
        List<Billet> list = new ArrayList<>();

        // REQUÊTE ROBUSTE AVEC LEFT JOIN
        String sql = """
            SELECT b.*, c.nom as cat_nom, e.nom as evt_nom
            FROM billet b
            LEFT JOIN categorie c ON b.categorie_id = c.id
            LEFT JOIN evenement e ON c.evenement_id = e.id
            WHERE b.utilisateur_id = ?
            ORDER BY b.date_emission DESC
        """;

        try {
            Connection conn = DBConnection.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, utilisateurId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Billet b = map(rs);

                        String nomCat = rs.getString("cat_nom");
                        String nomEvt = rs.getString("evt_nom");

                        b.setNomCategorie(nomCat);
                        b.setNomEvenement(nomEvt);

                        System.out.println("✅ DEBUG DAO Billet: " + nomEvt + " | " + nomCat);

                        list.add(b);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la recherche des billets : " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Billet findById(long id) {
        String sql = """
            SELECT b.*, c.nom as cat_nom, e.nom as evt_nom
            FROM billet b
            LEFT JOIN categorie c ON b.categorie_id = c.id
            LEFT JOIN evenement e ON c.evenement_id = e.id
            WHERE b.id = ?
        """;

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Billet b = map(rs);
                        b.setNomCategorie(rs.getString("cat_nom"));
                        b.setNomEvenement(rs.getString("evt_nom"));
                        return b;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Billet save(Billet b) {
        String sql = "INSERT INTO billet(paiement_id, utilisateur_id, categorie_id, numero_billet, code_qr, prix) VALUES(?,?,?,?,?,?) RETURNING id";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, b.getPaiementId());
                ps.setLong(2, b.getUtilisateurId());
                ps.setLong(3, b.getCategorieId());
                ps.setString(4, b.getNumeroBillet());
                ps.setString(5, b.getCodeQr());
                ps.setBigDecimal(6, b.getPrix());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) b.setId(rs.getLong(1));
                }
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Billet> findByPaiementId(long paiementId) {
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet WHERE paiement_id=?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, paiementId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public boolean marquerCommeUtilise(long id) {
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE billet SET statut='UTILISE', date_utilisation=NOW() WHERE id=? AND statut='VALIDE'")) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { return false; }
    }

    private Billet map(ResultSet rs) throws SQLException {
        Billet b = new Billet();
        b.setId(rs.getLong("id"));
        b.setPaiementId(rs.getLong("paiement_id"));
        b.setUtilisateurId(rs.getLong("utilisateur_id"));
        b.setCategorieId(rs.getLong("categorie_id"));
        b.setNumeroBillet(rs.getString("numero_billet"));
        b.setCodeQr(rs.getString("code_qr"));
        b.setPrix(rs.getBigDecimal("prix"));
        b.setStatut(rs.getString("statut"));

        Timestamp em = rs.getTimestamp("date_emission");
        if (em != null) b.setDateEmission(ZonedDateTime.ofInstant(em.toInstant(), java.time.ZoneId.systemDefault()));

        Timestamp ut = rs.getTimestamp("date_utilisation");
        if (ut != null) b.setDateUtilisation(ZonedDateTime.ofInstant(ut.toInstant(), java.time.ZoneId.systemDefault()));

        return b;
    }
}