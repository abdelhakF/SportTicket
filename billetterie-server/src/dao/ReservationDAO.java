package dao;

import Model.Reservation;
import java.util.List;

public interface ReservationDAO {

    Reservation save(Reservation r);

    Reservation findById(long id);

    // Méthodes de recherche
    List<Reservation> findEnAttenteByUtilisateur(long utilisateurId);

    List<Reservation> findAllEnAttente();

    List<Reservation> findExpirees();

    // Cette méthode manquait peut-être dans votre interface
    List<Reservation> findConfirmeesByUtilisateur(long utilisateurId);

    // Mises à jour / Suppressions
    boolean updateStatut(long id, String statut);

    boolean delete(long id);

    int nettoyerReservationsExpirees();
}