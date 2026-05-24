// service/TicketServiceImpl.java
package service;

import dao.*;
import impl.*;
import Model.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TicketServiceImpl extends UnicastRemoteObject implements TicketService {

    // Tous les DAO créés une seule fois
    private final UtilisateurDAO userDao;
    private final SportDAO sportDao;
    private final StadeDAO stadeDao;
    private final EvenementDAO eventDao;
    private final CategorieDAO catDao;
    private final PaiementDAO payDao;
    private final ReservationDAO resDao;
    private final BilletDAO billetDao;
    private final MailSender emailService;

    public TicketServiceImpl() throws RemoteException, SQLException {
        super();
        this.userDao = new UtilisateurDaoImpl();
        this.sportDao = new SportDaoImpl();
        this.stadeDao = new StadeDaoImpl();
        this.eventDao = new EvenementDaoImpl();
        this.catDao = new CategorieDaoImpl();
        this.payDao = new PaiementDaoImpl();
        this.resDao = new ReservationDaoImpl();
        this.billetDao = new BilletDaoImpl();
        this.emailService = new MailSender();
    }

    // ==================== UTILISATEURS ====================

    @Override
    public Utilisateur inscription(Utilisateur user) throws RemoteException {
        if (userDao.findByEmail(user.getEmail()) != null)
            return null;
        user.setRole("CLIENT");
        return userDao.save(user);
    }

    @Override
    public Utilisateur connexion(String email, String mdp) throws RemoteException {
        Utilisateur u = userDao.findByEmail(email);
        if (u != null && u.getMotDePasse().equals(mdp) && "ACTIF".equals(u.getStatut())) {
            return u;
        }
        return null;
    }

    @Override
    public boolean deconnexion(long userId) throws RemoteException {
        return true;
    }

    @Override
    public boolean modifierProfil(Utilisateur user) throws RemoteException {
        try {
            Utilisateur existant = userDao.findById(user.getId());
            if (existant == null || !"CLIENT".equals(existant.getRole())) {
                return false;
            }
            existant.setNom(user.getNom());
            existant.setPrenom(user.getPrenom());
            existant.setTelephone(user.getTelephone());
            if (user.getMotDePasse() != null && !user.getMotDePasse().trim().isEmpty()) {
                existant.setMotDePasse(user.getMotDePasse());
            }
            userDao.save(existant);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== ADMIN & GESTION ====================

    @Override
    public List<Utilisateur> listerUtilisateurs(String recherche) throws RemoteException {
        return recherche == null || recherche.isBlank() ? userDao.findAll() : userDao.searchByNomOrEmail(recherche);
    }

    @Override
    public boolean bloquerUtilisateur(long id, boolean bloquer) throws RemoteException {
        return userDao.updateStatut(id, bloquer ? "BLOQUE" : "ACTIF");
    }

    @Override
    public List<Sport> rechercherSports(String keyword) throws RemoteException {
        return keyword == null || keyword.isBlank() ? sportDao.findAll() : sportDao.searchByNom(keyword);
    }

    @Override
    public List<Stade> rechercherStades(String keyword) throws RemoteException {
        return keyword == null || keyword.isBlank() ? stadeDao.findAll() : stadeDao.search(keyword);
    }

    @Override
    public List<Evenement> rechercherEvenements(String keyword) throws RemoteException {
        return keyword == null || keyword.isBlank() ? eventDao.findAll() : eventDao.search(keyword);
    }

    @Override
    public Evenement creerEvenement(Evenement e) throws RemoteException {
        return eventDao.save(e);
    }

    @Override
    public Categorie ajouterCategorie(Categorie c) throws RemoteException {
        try {
            List<Categorie> existantes = catDao.findByEvenementId(c.getEvenementId());
            boolean existe = existantes.stream()
                    .anyMatch(cat -> cat.getNom().equalsIgnoreCase(c.getNom()));
            if (existe)
                return null;
            return catDao.save(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BigDecimal getCA() throws RemoteException {
        return payDao.getChiffreAffairesTotal();
    }

    @Override
    public List<Object[]> getCAParEvenement() throws RemoteException {
        return payDao.getCAParEvenement();
    }

    @Override
    public Evenement creerEvenementComplet(String nomEvenement, String dateHeure, String nomSport, String nomStade,
            String villeStade, int capaciteStade) throws RemoteException {
        // Validation des paramètres
        if (nomEvenement == null || nomEvenement.trim().isEmpty()) {
            throw new RemoteException("Le nom de l'événement est requis");
        }
        if (nomSport == null || nomSport.trim().isEmpty()) {
            throw new RemoteException("Le nom du sport est requis");
        }
        if (nomStade == null || nomStade.trim().isEmpty()) {
            throw new RemoteException("Le nom du stade est requis");
        }
        if (villeStade == null || villeStade.trim().isEmpty()) {
            throw new RemoteException("La ville du stade est requise");
        }
        if (capaciteStade <= 0) {
            throw new RemoteException("La capacité du stade doit être supérieure à 0");
        }

        // Parsing de la date avec plusieurs formats supportés
        ZonedDateTime date = null;
        String[] dateFormats = {
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy HH:mm",
                "dd/MM/yyyy HH:mm:ss"
        };

        for (String format : dateFormats) {
            try {
                date = LocalDateTime.parse(dateHeure.trim(), DateTimeFormatter.ofPattern(format))
                        .atZone(ZoneId.of("Europe/Paris"));
                break;
            } catch (Exception e) {
                // Continue avec le format suivant
            }
        }

        if (date == null) {
            throw new RemoteException("Format de date invalide. Formats acceptés: yyyy-MM-dd HH:mm, dd/MM/yyyy HH:mm");
        }

        // Vérifier que la date est dans le futur
        if (date.isBefore(ZonedDateTime.now())) {
            throw new RemoteException("La date de l'événement doit être dans le futur");
        }

        // Gestion optimisée du Sport (création ou récupération)
        Sport sport = getOrCreateSport(nomSport.trim());
        if (sport == null) {
            throw new RemoteException("Impossible de créer ou récupérer le sport : " + nomSport);
        }

        // Gestion optimisée du Stade (création ou récupération)
        Stade stade = getOrCreateStade(nomStade.trim(), villeStade.trim(), capaciteStade);
        if (stade == null) {
            throw new RemoteException("Impossible de créer ou récupérer le stade : " + nomStade);
        }

        // Vérifier qu'il n'y a pas déjà un événement à la même date dans le même stade
        // (La contrainte UNIQUE dans la base de données devrait gérer cela, mais on
        // vérifie quand même)

        // Création de l'événement
        Evenement nouvelEvenement = new Evenement(nomEvenement.trim(), date, sport.getId(), stade.getId());
        Evenement evenementCree = eventDao.save(nouvelEvenement);

        if (evenementCree == null) {
            throw new RemoteException("Erreur lors de la création de l'événement dans la base de données");
        }

        // --- FEATURE 3 : CRÉATION AUTOMATIQUE DES CATÉGORIES ---
        try {
            // 1. Catégorie STANDARD (70% des places, Prix de base 50)
            int placesStandard = (int) (capaciteStade * 0.7);
            Categorie catStandard = new Categorie(evenementCree.getId(), "Standard", new BigDecimal("50.00"),
                    placesStandard);
            catDao.save(catStandard);

            // 2. Catégorie VIP (30% des places, Prix 100)
            int placesVip = capaciteStade - placesStandard;
            Categorie catVip = new Categorie(evenementCree.getId(), "VIP", new BigDecimal("100.00"), placesVip);
            catDao.save(catVip);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création automatique des catégories : " + e.getMessage());
            // On ne bloque pas la création de l'événement pour autant, mais c'est à noter
        }
        // -------------------------------------------------------

        return evenementCree;
    }

    @Override
    public boolean deleteEvenement(long id) throws RemoteException {
        return eventDao.delete(id);
    }

    /**
     * Récupère un sport existant ou le crée s'il n'existe pas
     */
    private Sport getOrCreateSport(String nomSport) {
        if (nomSport == null || nomSport.trim().isEmpty()) {
            return null;
        }

        String nomSportTrim = nomSport.trim();
        String nomSportCapitalise = capitaliser(nomSportTrim);

        // Tentative 1: Recherche avec le nom original (insensible à la casse)
        Sport sport = sportDao.findByNom(nomSportTrim);
        if (sport != null) {
            return sport;
        }

        // Tentative 2: Recherche avec le nom capitalisé
        sport = sportDao.findByNom(nomSportCapitalise);
        if (sport != null) {
            return sport;
        }

        // Tentative 3: Création du sport
        try {
            sport = sportDao.save(new Sport(nomSportCapitalise));
            if (sport != null) {
                return sport;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du sport: " + e.getMessage());
        }

        // Tentative 4: Dernière recherche après création (cas de race condition)
        sport = sportDao.findByNom(nomSportCapitalise);
        if (sport != null) {
            return sport;
        }

        return sportDao.findByNom(nomSportTrim);
    }

    /**
     * Récupère un stade existant ou le crée s'il n'existe pas
     */
    private Stade getOrCreateStade(String nomStade, String villeStade, int capacite) {
        if (nomStade == null || nomStade.trim().isEmpty() ||
                villeStade == null || villeStade.trim().isEmpty()) {
            return null;
        }

        String nomStadeTrim = nomStade.trim();
        String villeStadeTrim = villeStade.trim();
        String nomStadeCapitalise = capitaliser(nomStadeTrim);
        String villeStadeCapitalise = capitaliser(villeStadeTrim);

        // Tentative 1: Recherche avec les noms originaux (insensible à la casse)
        Stade stade = stadeDao.findByNomAndVille(nomStadeTrim, villeStadeTrim);
        if (stade != null) {
            // Mise à jour de la capacité si elle a changé
            if (stade.getCapacite() != capacite) {
                stade.setCapacite(capacite);
                stade = stadeDao.save(stade);
            }
            return stade;
        }

        // Tentative 2: Recherche avec les noms capitalisés
        stade = stadeDao.findByNomAndVille(nomStadeCapitalise, villeStadeCapitalise);
        if (stade != null) {
            if (stade.getCapacite() != capacite) {
                stade.setCapacite(capacite);
                stade = stadeDao.save(stade);
            }
            return stade;
        }

        // Tentative 3: Création du stade
        try {
            stade = stadeDao.save(new Stade(nomStadeCapitalise, villeStadeCapitalise, capacite));
            if (stade != null) {
                return stade;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du stade: " + e.getMessage());
        }

        // Tentative 4: Dernière recherche après création (cas de race condition)
        stade = stadeDao.findByNomAndVille(nomStadeCapitalise, villeStadeCapitalise);
        if (stade != null) {
            return stade;
        }

        return stadeDao.findByNomAndVille(nomStadeTrim, villeStadeTrim);
    }

    private String capitaliser(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // ==================== COEUR DU SYSTÈME (Réservation & Nettoyage)
    // ====================

    @Override
    public int nettoyerReservationsExpirees() throws RemoteException {
        // 1. Récupère les réservations expirées via le DAO (qui compare avec
        // LocalDateTime.now())
        List<Reservation> expirees = resDao.findExpirees();
        int count = 0;
        for (Reservation r : expirees) {
            System.out.println(
                    "NETTOYAGE : Réservation " + r.getId() + " expirée. Libération de " + r.getQuantite() + " places.");

            // 2. Libère les places : on soustrait la quantité des "places_reservees"
            catDao.updatePlaces(r.getCategorieId(), -r.getQuantite(), 0);

            // 3. Marque comme expirée
            resDao.updateStatut(r.getId(), "EXPIREE");
            count++;
        }
        return count;
    }

    @Override
    public List<Evenement> getEvenementsAVenir() throws RemoteException {
        // AUTO-NETTOYAGE : On vérifie si des places doivent être libérées maintenant
        this.nettoyerReservationsExpirees();

        List<Evenement> events = new ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE statut = 'A_VENIR' ORDER BY date_evenement";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            ZonedDateTime now = ZonedDateTime.now();
            while (rs.next()) {
                OffsetDateTime odt = rs.getObject("date_evenement", OffsetDateTime.class);
                if (odt == null)
                    continue;

                ZonedDateTime evenementDate = odt.atZoneSameInstant(ZoneId.systemDefault());
                if (evenementDate.isAfter(now)) {
                    Evenement e = new Evenement();
                    e.setId(rs.getLong("id"));
                    e.setNom(rs.getString("nom"));
                    e.setDateEvenement(evenementDate);
                    e.setSportId(rs.getLong("sport_id"));
                    e.setStadeId(rs.getLong("stade_id"));
                    e.setStatut(rs.getString("statut"));
                    events.add(e);
                }
            }
        } catch (SQLException ex) {
            throw new RemoteException("Erreur DB dans getEvenementsAVenir", ex);
        }
        return events;
    }

    @Override
    public List<Categorie> getCategoriesDisponibles(long evenementId) throws RemoteException {
        // AUTO-NETTOYAGE : Avant d'afficher les stocks, on libère ce qui est expiré
        this.nettoyerReservationsExpirees();

        return catDao.findByEvenementId(evenementId).stream()
                .filter(c -> c.getPlacesDisponibles() > 0)
                .toList();
    }

    @Override
    public List<Reservation> getReservationsEnAttente(long utilisateurId) throws RemoteException {
        // AUTO-NETTOYAGE : Avant d'afficher le panier, on retire ce qui est expiré
        this.nettoyerReservationsExpirees();

        return resDao.findEnAttenteByUtilisateur(utilisateurId);
    }

    @Override
    public Reservation reserverPlaces(long utilisateurId, long categorieId, int quantite) throws RemoteException {
        Categorie cat = catDao.findById(categorieId);
        if (cat == null || cat.getPlacesDisponibles() < quantite)
            return null;

        Reservation r = new Reservation(utilisateurId, categorieId, quantite, cat.getPrix());

        // DÉLAI DE 2 MINUTES
        r.setDateExpiration(ZonedDateTime.now().plusMinutes(2));

        r = resDao.save(r);

        // Bloque les places
        catDao.updatePlaces(categorieId, quantite, 0);
        return r;
    }

    @Override
    public boolean annulerReservation(long reservationId) throws RemoteException {
        Reservation r = resDao.findById(reservationId);
        if (r == null || !"EN_ATTENTE".equals(r.getStatut()))
            return false;

        resDao.updateStatut(reservationId, "ANNULEE");
        // Libère les places manuellement si le client annule
        catDao.updatePlaces(r.getCategorieId(), -r.getQuantite(), 0);
        return true;
    }

    @Override
    public Paiement payerReservation(long reservationId, String methode) throws RemoteException {
        Reservation r = resDao.findById(reservationId);
        if (r == null || !"EN_ATTENTE".equals(r.getStatut()))
            return null;

        BigDecimal montant = r.getPrixUnitaire().multiply(new BigDecimal(r.getQuantite()));

        Paiement p = new Paiement((long) reservationId, r.getUtilisateurId(), montant, methode,
                "REF-" + System.currentTimeMillis());
        p = payDao.save(p);

        resDao.updateStatut(reservationId, "PAYEE");
        catDao.updatePlaces(r.getCategorieId(), -r.getQuantite(), r.getQuantite());

        // Liste pour stocker les billets générés
        List<Billet> billetsGeneres = new ArrayList<>();

        for (int i = 0; i < r.getQuantite(); i++) {
            Billet b = new Billet(p.getId(), r.getUtilisateurId(), r.getCategorieId(), r.getPrixUnitaire());
            b = billetDao.save(b); // On récupère le billet sauvegardé (avec son ID/Numéro)
            billetsGeneres.add(b);
        }

        // --- ENVOI DE L'EMAIL ---
        try {
            // 1. Récupérer l'utilisateur
            Utilisateur user = userDao.findById(r.getUtilisateurId());

            // 2. Récupérer le nom de l'événement (un peu de gymnastique SQL ou DAO)
            Categorie cat = catDao.findById(r.getCategorieId()); // On récupère la catégorie
            // Note: Idéalement il faudrait eventDao.findById(cat.getEvenementId()),
            // mais pour faire simple on envoie un titre générique ou on fait la requête :
            Evenement evt = eventDao.findById(cat.getEvenementId());

            // 3. Envoyer
            if (user != null) {
                emailService.envoyerBillets(user, billetsGeneres, (evt != null ? evt.getNom() : "Votre Événement"));
            }
        } catch (Exception e) {
            System.err.println("Erreur non bloquante lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
        // ------------------------

        return p;
    }

    @Override
    public List<Billet> getBilletsClient(long utilisateurId) throws RemoteException {
        return billetDao.findByUtilisateurId(utilisateurId);
    }
}