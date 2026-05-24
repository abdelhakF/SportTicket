// dao/TicketService.java (côté serveur ET client)
package service;

import Model.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.math.BigDecimal;
import java.util.List;

public interface TicketService extends Remote {

    // TicketService.java (interface RMI)
    Evenement creerEvenementComplet(
            String nomEvenement,
            String dateHeure, // format yyyy-MM-dd HH:mm
            String nomSport,
            String nomStade,
            String villeStade,
            int capaciteStade) throws RemoteException;

    boolean deleteEvenement(long id) throws RemoteException;

    // Méthodes communes
    Utilisateur inscription(Utilisateur user) throws RemoteException;

    Utilisateur connexion(String email, String mdp) throws RemoteException;

    boolean deconnexion(long userId) throws RemoteException;

    // Méthodes ADMIN
    List<Utilisateur> listerUtilisateurs(String recherche) throws RemoteException;

    boolean bloquerUtilisateur(long id, boolean bloquer) throws RemoteException;

    List<Sport> rechercherSports(String keyword) throws RemoteException;

    List<Stade> rechercherStades(String keyword) throws RemoteException;

    List<Evenement> rechercherEvenements(String keyword) throws RemoteException;

    Evenement creerEvenement(Evenement e) throws RemoteException;

    Categorie ajouterCategorie(Categorie c) throws RemoteException;

    BigDecimal getCA() throws RemoteException;

    List<Object[]> getCAParEvenement() throws RemoteException;

    int nettoyerReservationsExpirees() throws RemoteException; // ← C'était celle qui manquait !

    // === CLIENT ===
    List<Evenement> getEvenementsAVenir() throws RemoteException;

    List<Categorie> getCategoriesDisponibles(long evenementId) throws RemoteException;

    Reservation reserverPlaces(long utilisateurId, long categorieId, int quantite) throws RemoteException;

    List<Reservation> getReservationsEnAttente(long utilisateurId) throws RemoteException;

    boolean annulerReservation(long reservationId) throws RemoteException;

    Paiement payerReservation(long reservationId, String methode) throws RemoteException;

    List<Billet> getBilletsClient(long utilisateurId) throws RemoteException;

    boolean modifierProfil(Utilisateur user) throws RemoteException;
}