package dao;

import Model.Utilisateur;
import java.util.List;

public interface UtilisateurDAO {
    Utilisateur findByEmail(String email);
    Utilisateur save(Utilisateur user);
    Utilisateur findById(long id);
    boolean updateStatut(long id, String statut);

    // NOUVELLES MÉTHODES SANS ID
    List<Utilisateur> findAll();
    List<Utilisateur> searchByNomOrEmail(String keyword);
}