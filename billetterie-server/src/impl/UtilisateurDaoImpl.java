package impl;

import dao.DBConnection;
import dao.UtilisateurDAO;
import Model.Utilisateur;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDaoImpl implements UtilisateurDAO {

    // Constructeur vide (Plus de connexion stockée ici)
    public UtilisateurDaoImpl() {
    }

    @Override
    public Utilisateur findByEmail(String email) {
        try {
            Connection conn = DBConnection.getConnection(); // Connexion fraîche
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM utilisateur WHERE email = ?")) {
                ps.setString(1, email);
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
    public Utilisateur findById(long id) {
        try {
            Connection conn = DBConnection.getConnection(); // Connexion fraîche
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM utilisateur WHERE id = ?")) {
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
    public Utilisateur save(Utilisateur user) {
        // CORRECTION IMPORTANTE : Gestion de l'UPDATE si l'utilisateur existe déjà (ID != 0)
        // Sinon la modification de profil plantera ou créera un doublon.

        if (user.getId() == 0) {
            // --- CAS INSERTION (Nouveau compte) ---
            String sql = "INSERT INTO utilisateur(nom, prenom, email, mot_de_passe, telephone, role, statut, date_inscription) " +
                    "VALUES(?,?,?,?,?,?,?, NOW()) RETURNING id";

            try {
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user.getNom());
                    ps.setString(2, user.getPrenom());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getMotDePasse());
                    ps.setString(5, user.getTelephone());

                    // Logique métier : Role par défaut
                    String role = (user.getRole() == null || user.getRole().trim().isEmpty() || !"ADMIN".equals(user.getRole())) ? "CLIENT" : "ADMIN";
                    ps.setString(6, role);

                    // Statut par défaut
                    ps.setString(7, "ACTIF");

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            user.setId(rs.getLong(1));
                            user.setStatut("ACTIF");
                            user.setRole(role);
                            user.setDateInscription(ZonedDateTime.now());
                            return user;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // --- CAS MISE À JOUR (Modification profil) ---
            String sql = "UPDATE utilisateur SET nom=?, prenom=?, telephone=?, mot_de_passe=? WHERE id=?";
            try {
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user.getNom());
                    ps.setString(2, user.getPrenom());
                    ps.setString(3, user.getTelephone());
                    ps.setString(4, user.getMotDePasse());
                    ps.setLong(5, user.getId());
                    ps.executeUpdate();
                    return user;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean updateStatut(long id, String statut) {
        try {
            Connection conn = DBConnection.getConnection(); // Connexion fraîche
            try (PreparedStatement ps = conn.prepareStatement("UPDATE utilisateur SET statut=? WHERE id=?")) {
                ps.setString(1, statut);
                ps.setLong(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection(); // Connexion fraîche
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM utilisateur ORDER BY nom, prenom")) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Utilisateur> searchByNomOrEmail(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAll();
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE LOWER(nom) LIKE LOWER(?) OR LOWER(prenom) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) ORDER BY nom";

        try {
            Connection conn = DBConnection.getConnection(); // Connexion fraîche
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Utilisateur map(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setTelephone(rs.getString("telephone"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));

        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null) u.setDateInscription(ZonedDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()));
        return u;
    }
}