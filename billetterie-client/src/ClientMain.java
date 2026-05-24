import Model.*;
import service.TicketService;

import java.math.BigDecimal;
import java.rmi.Naming;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ClientMain {

    private static final Scanner sc = new Scanner(System.in);
    private static TicketService service;
    private static Utilisateur utilisateurConnecte = null;
    private static final String SERVICE_NAME = "TicketService";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        // Connexion au serveur RMI
        try {
            service = (TicketService) Naming.lookup("rmi://localhost:1099/" + SERVICE_NAME);
            System.out.println("Connexion au serveur RMI réussie !");
        } catch (Exception e) {
            System.err.println("Impossible de se connecter au serveur RMI. Vérifie que le serveur est lancé.");
            e.printStackTrace();
            return;
        }

        boolean continuer = true;
        while (continuer) {
            if (utilisateurConnecte == null) {
                menuNonConnecte();
            } else if ("ADMIN".equals(utilisateurConnecte.getRole())) {
                menuAdmin();
            } else {
                menuClient();
            }
        }
        sc.close();
    }

    // ==================== MENU NON CONNECTÉ ====================
    private static void menuNonConnecte() {
        System.out.println("\n=== SYSTÈME DE TICKETING ===");
        System.out.println("1. Inscription");
        System.out.println("2. Connexion");
        System.out.println("0. Quitter");
        System.out.print("Choix : ");

        switch (lireChoix()) {
            case 1 -> inscription();
            case 2 -> connexion();
            case 0 -> {
                System.out.println("Au revoir !");
                System.exit(0);
            }
            default -> System.out.println("Choix invalide.");
        }
    }

    // ==================== MENU CLIENT ====================
    private static void menuClient() {
        System.out.printf("\n=== CLIENT : %s %s ===%n", utilisateurConnecte.getPrenom(), utilisateurConnecte.getNom());
        System.out.println("1. Voir les événements à venir");
        System.out.println("2. Mes réservations en cours (panier)");
        System.out.println("3. Mes billets");
        System.out.println("4. Modifier mon profil");
        System.out.println("9. Déconnexion");
        System.out.println("0. Quitter");
        System.out.print("Choix : ");

        switch (lireChoix()) {
            case 1 -> voirEvenementsAVenir();
            case 2 -> voirPanier();
            case 3 -> voirMesBillets();
            case 4 -> modifierProfil();
            case 9 -> {
                utilisateurConnecte = null;
                System.out.println("Déconnexion réussie.");
            }
            case 0 -> System.exit(0);
            default -> System.out.println("Choix invalide.");
        }
    }

    // ==================== MENU ADMIN ====================
    private static void menuAdmin() {
        System.out.printf("\n=== ADMINISTRATEUR : %s %s ===%n", utilisateurConnecte.getPrenom(), utilisateurConnecte.getNom());
        System.out.println("1. Lister les utilisateurs");
        System.out.println("2. Bloquer / Débloquer un utilisateur");
        System.out.println("3. Créer un événement");
        System.out.println("4. Ajouter des catégories à un événement");
        System.out.println("5. Voir tous les événements");
        System.out.println("6. Chiffre d'affaires total");
        System.out.println("7. CA par événement");
        System.out.println("9. Déconnexion");
        System.out.println("0. Quitter");
        System.out.print("Choix : ");

        switch (lireChoix()) {
            case 1 -> listerUtilisateurs();
            case 2 -> bloquerDebloquerUtilisateur();
            case 3 -> creerEvenement();
            case 4 -> ajouterCategoriesEvenement();
            case 5 -> listerEvenements();
            case 6 -> voirChiffreAffairesTotal();
            case 7 -> voirCAParEvenement();
            case 9 -> {
                utilisateurConnecte = null;
                System.out.println("Déconnexion.");
            }
            case 0 -> System.exit(0);
            default -> System.out.println("Choix invalide.");
        }
    }

    // ==================== FONCTIONS CLIENT ====================
    private static void voirEvenementsAVenir() {
        try {
            List<Evenement> events = service.getEvenementsAVenir();
            if (events.isEmpty()) {
                System.out.println("Aucun événement à venir.");
                return;
            }

            System.out.println("\n=== ÉVÉNEMENTS À VENIR ===");
            for (int i = 0; i < events.size(); i++) {
                Evenement e = events.get(i);
                System.out.printf("%d. %s%n   %s | ID: %d%n",
                        i + 1, e.getNom(),
                        e.getDateEvenement().format(DATE_FORMAT), e.getId());
            }

            System.out.print("Voir le détail (numéro) ou 0 pour retour : ");
            int choix = lireChoix();
            if (choix > 0 && choix <= events.size()) {
                detailEvenement(events.get(choix - 1));
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des événements.");
        }
    }

    private static void detailEvenement(Evenement e) {
        try {
            System.out.printf("%n=== %s ===%n", e.getNom());
            System.out.printf("Date : %s%n", e.getDateEvenement().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy à HH:mm", java.util.Locale.FRENCH)));

            List<Categorie> cats = service.getCategoriesDisponibles(e.getId());
            if (cats.isEmpty()) {
                System.out.println("Aucune place disponible pour cet événement.");
                return;
            }

            System.out.println("Catégories disponibles :");
            for (int i = 0; i < cats.size(); i++) {
                Categorie c = cats.get(i);
                System.out.printf("%d. %s → %.2f € | %d places restantes%n",
                        i + 1, c.getNom(), c.getPrix(), c.getPlacesDisponibles());
            }

            System.out.print("Réserver une catégorie (numéro) ou 0 pour retour : ");
            int choix = lireChoix();
            if (choix > 0 && choix <= cats.size()) {
                reserverPlaces(e, cats.get(choix - 1));
            }
        } catch (Exception ex) {
            System.out.println("Erreur lors du détail de l'événement.");
        }
    }

    private static void reserverPlaces(Evenement e, Categorie cat) {
        System.out.printf("Catégorie : %s - %.2f €%n", cat.getNom(), cat.getPrix());
        System.out.printf("Places disponibles : %d%n", cat.getPlacesDisponibles());
        System.out.print("Quantité souhaitée : ");
        int qty = lireChoix();

        if (qty <= 0 || qty > cat.getPlacesDisponibles()) {
            System.out.println("Quantité invalide ou insuffisante.");
            return;
        }

        try {
            Reservation r = service.reserverPlaces(utilisateurConnecte.getId(), cat.getId(), qty);
            if (r != null) {
                System.out.printf("Réservé ! %d place(s) × %.2f € = %.2f €%n", qty, cat.getPrix(), qty * cat.getPrix().doubleValue());
                System.out.println("Vous avez 2 minutes pour payer !");
            } else {
                System.out.println("Plus de places disponibles !");
            }
        } catch (Exception ex) {
            System.out.println("Erreur lors de la réservation.");
        }
    }

    private static void voirPanier() {
        try {
            List<Reservation> panier = service.getReservationsEnAttente(utilisateurConnecte.getId());
            if (panier.isEmpty()) {
                System.out.println("Votre panier est vide.");
                return;
            }

            System.out.println("\n=== MON PANIER ===");
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < panier.size(); i++) {
                Reservation r = panier.get(i);
                long minutesRestantes = Duration.between(ZonedDateTime.now(), r.getDateExpiration()).toMinutes();
                // Use the nomCategorie field directly from the reservation object
                String nomCategorie = (r.getNomCategorie() != null && !r.getNomCategorie().trim().isEmpty()) 
                        ? r.getNomCategorie() : "Catégorie inconnue";

                System.out.printf("%d. %s × %d → %.2f € (expire dans %d min)%n",
                        i + 1,
                        nomCategorie,
                        r.getQuantite(),
                        r.getQuantite() * r.getPrixUnitaire().doubleValue(),
                        minutesRestantes);
                total = total.add(r.getPrixUnitaire().multiply(BigDecimal.valueOf(r.getQuantite())));
            }
            System.out.printf("%nTOTAL À PAYER : %.2f €%n", total);

            System.out.print("Payer maintenant ? (o/n) : ");
            if (sc.nextLine().trim().equalsIgnoreCase("o")) {
                payerPanier(panier);
            }
        } catch (Exception e) {
            System.out.println("Erreur panier.");
        }
    }

    private static void payerPanier(List<Reservation> panier) {
        System.out.println("Simulation de paiement en cours...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        boolean toutOk = true;
        for (Reservation r : panier) {
            try {
                service.payerReservation(r.getId(), "CB");
            } catch (Exception e) {
                toutOk = false;
            }
        }

        if (toutOk) {
            System.out.println("PAIEMENT ACCEPTÉ ! Vos billets arrivent par email.");
        } else {
            System.out.println("Erreur lors du paiement.");
        }
    }

    private static void voirMesBillets() {
        try {
            // 1. Le serveur envoie déjà une liste de Billets avec les noms dedans
            List<Billet> billets = service.getBilletsClient(utilisateurConnecte.getId());

            if (billets.isEmpty()) {
                System.out.println("Vous n'avez aucun billet.");
                return;
            }
            System.out.println("\n=== MES BILLETS ===");

            for (Billet b : billets) {

                // --- CORRECTION : UTILISATION DIRECTE DES CHAMPS DU BILLET ---
                // Le serveur garantit que ces champs sont remplis (voir log serveur)
                String nomEvent = b.getNomEvenement();
                String nomCat = b.getNomCategorie();

                // On utilise la logique de la méthode toString() pour afficher 'Inconnu' si les champs sont nulls
                // (La méthode toString() de Billet.java fait déjà ça si vous l'utilisez)
                String evtAffichage = (nomEvent != null) ? nomEvent : "Événement inconnu [Erreur RMI]";
                String catAffichage = (nomCat != null) ? nomCat : "Catégorie inconnue [Erreur RMI]";

                String qrCode = b.getCodeQr();
                String qrDisplay = (qrCode != null && qrCode.length() > 10) 
                        ? qrCode.substring(0, 10) + "..." 
                        : (qrCode != null ? qrCode : "N/A");
                System.out.printf("• %s | %s | %.2f € | Émis le %s | QR: %s%n",
                        evtAffichage,
                        catAffichage,
                        b.getPrix(),
                        b.getDateEmission().format(DATE_FORMAT),
                        qrDisplay);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des billets.");
            e.printStackTrace(); // Laissez le stack trace pour voir l'erreur RMI
        }
    }

    private static void modifierProfil() {
        System.out.println("\n=== MODIFIER MON PROFIL ===");
        System.out.print("Nouveau nom (" + utilisateurConnecte.getNom() + ") : ");
        String nom = sc.nextLine().trim();
        if (nom.isEmpty()) nom = utilisateurConnecte.getNom();

        System.out.print("Nouveau prénom (" + utilisateurConnecte.getPrenom() + ") : ");
        String prenom = sc.nextLine().trim();
        if (prenom.isEmpty()) prenom = utilisateurConnecte.getPrenom();

        System.out.print("Nouveau téléphone (" + (utilisateurConnecte.getTelephone() != null ? utilisateurConnecte.getTelephone() : "aucun") + ") : ");
        String tel = sc.nextLine().trim();

        System.out.print("Nouveau mot de passe (laisser vide pour garder) : ");
        String mdp = sc.nextLine();

        Utilisateur updated = new Utilisateur();
        updated.setId(utilisateurConnecte.getId());
        updated.setNom(nom);
        updated.setPrenom(prenom);
        updated.setTelephone(tel.isEmpty() ? null : tel);
        if (!mdp.isEmpty()) {
            updated.setMotDePasse(mdp);
        }

        try {
            if (service.modifierProfil(updated)) {
                // On reconnecte pour rafraîchir les données
                utilisateurConnecte = service.connexion(
                        utilisateurConnecte.getEmail(),
                        mdp.isEmpty() ? utilisateurConnecte.getMotDePasse() : mdp
                );
                System.out.println("Profil mis à jour avec succès !");
            } else {
                System.out.println("Échec de la mise à jour.");
            }
        } catch (Exception e) {
            System.out.println("Erreur serveur lors de la mise à jour.");
            e.printStackTrace();
        }
    }
    // ==================== FONCTIONS COMMUNES ====================
    private static void inscription() {
        System.out.println("\n--- INSCRIPTION ---");
        System.out.print("Nom : "); String nom = sc.nextLine();
        System.out.print("Prénom : "); String prenom = sc.nextLine();
        System.out.print("Email : "); String email = sc.nextLine();
        System.out.print("Mot de passe : "); String mdp = sc.nextLine();
        System.out.print("Téléphone (optionnel) : "); String tel = sc.nextLine();

        Utilisateur u = new Utilisateur(nom, prenom, email, mdp, tel.isEmpty() ? null : tel, "CLIENT");

        try {
            Utilisateur inscrit = service.inscription(u);
            if (inscrit != null) {
                System.out.println("Inscription réussie ! Vous pouvez vous connecter.");
            } else {
                System.out.println("Échec : email déjà utilisé.");
            }
        } catch (Exception e) {
            System.out.println("Erreur serveur : " + e.getMessage());
        }
    }

    private static void connexion() {
        System.out.println("\n--- CONNEXION ---");
        System.out.print("Email : "); String email = sc.nextLine();
        System.out.print("Mot de passe : "); String mdp = sc.nextLine();

        try {
            utilisateurConnecte = service.connexion(email, mdp);
            if (utilisateurConnecte != null) {
                System.out.printf("Connexion réussie ! Bienvenue %s (%s)%n",
                        utilisateurConnecte.getPrenom(), utilisateurConnecte.getRole());
            } else {
                System.out.println("Identifiants incorrects ou compte bloqué.");
            }
        } catch (Exception e) {
            System.out.println("Erreur de connexion au serveur.");
            e.printStackTrace();
        }
    }

    // ==================== FONCTIONS ADMIN (via RMI) ====================
    private static void listerUtilisateurs() {
        System.out.print("Rechercher (nom/email, vide = tous) : ");
        String recherche = sc.nextLine();
        try {
            List<Utilisateur> users = service.listerUtilisateurs(recherche);
            if (users.isEmpty()) {
                System.out.println("Aucun utilisateur trouvé.");
                return;
            }
            for (int i = 0; i < users.size(); i++) {
                Utilisateur u = users.get(i);
                System.out.printf("%d. %s %s (%s) - %s%n", i+1, u.getNom(), u.getPrenom(), u.getEmail(), u.getStatut());
            }
        } catch (Exception e) { System.out.println("Erreur serveur."); }
    }

    private static void bloquerDebloquerUtilisateur() {
        System.out.print("Rechercher utilisateur : ");
        String recherche = sc.nextLine();
        try {
            List<Utilisateur> results = service.listerUtilisateurs(recherche);
            if (results.isEmpty()) { System.out.println("Aucun trouvé."); return; }
            for (int i = 0; i < results.size(); i++) {
                Utilisateur u = results.get(i);
                System.out.printf("%d. %s %s - %s%n", i+1, u.getNom(), u.getPrenom(), u.getStatut());
            }
            System.out.print("Numéro à modifier : ");
            int choix = lireChoix() - 1;
            if (choix >= 0 && choix < results.size()) {
                Utilisateur u = results.get(choix);
                boolean bloquer = "ACTIF".equals(u.getStatut());
                if (service.bloquerUtilisateur(u.getId(), bloquer)) {
                    System.out.println("Utilisateur " + (bloquer ? "bloqué" : "débloqué") + " !");
                }
            }
        } catch (Exception e) { System.out.println("Erreur."); }
    }

    private static void creerEvenement() {
        System.out.println("\n--- CRÉATION D'ÉVÉNEMENT ---");

        System.out.print("Nom de l'événement : ");
        String nom = sc.nextLine();

        System.out.print("Date et heure (yyyy-MM-dd HH:mm) : ");
        String date = sc.nextLine();

        System.out.print("Sport : ");
        String sport = sc.nextLine();

        System.out.print("Stade : ");
        String stade = sc.nextLine();

        System.out.print("Ville du stade : ");
        String ville = sc.nextLine();

        System.out.print("Capacité du stade : ");
        int capacite = Integer.parseInt(sc.nextLine());

        try {
            Evenement e = service.creerEvenementComplet(nom, date, sport, stade, ville, capacite);
            System.out.println("ÉVÉNEMENT CRÉÉ AVEC SUCCÈS ! ID = " + e.getId());
        } catch (Exception ex) {
            System.out.println("Erreur : " + ex.getMessage());
        }
    }

    private static void ajouterCategoriesEvenement() {
        System.out.print("Rechercher événement : ");
        try {
            List<Evenement> evts = service.rechercherEvenements(sc.nextLine());
            if (evts.isEmpty()) return;
            for (int i = 0; i < evts.size(); i++) System.out.printf("%d. %s%n", i+1, evts.get(i).getNom());
            int choix = lireChoix() - 1;
            if (choix < 0 || choix >= evts.size()) {
                System.out.println("Choix invalide.");
                return;
            }
            long eventId = evts.get(choix).getId();

            while (true) {
                System.out.print("Catégorie (vide = stop) : "); String nom = sc.nextLine();
                if (nom.isBlank()) break;
                System.out.print("Prix : "); BigDecimal prix = new BigDecimal(sc.nextLine());
                System.out.print("Places : "); int places = Integer.parseInt(sc.nextLine());
                service.ajouterCategorie(new Categorie(eventId, nom, prix, places));
                System.out.println("Catégorie ajoutée.");
            }
        } catch (Exception e) { System.out.println("Erreur."); }
    }

    private static void listerEvenements() { try { afficherListe(service.rechercherEvenements("")); } catch (Exception e) {} }
    private static void voirChiffreAffairesTotal() { try { System.out.println("CA Total : " + service.getCA() + " €"); } catch (Exception e) {} }
    private static void voirCAParEvenement() { try { service.getCAParEvenement().forEach(l -> System.out.printf("• %s → %.2f €%n", l[0], l[3])); } catch (Exception e) {} }
    // ==================== UTILITAIRES ====================
    private static int lireChoix() {
        try { return Integer.parseInt(sc.nextLine()); }
        catch (Exception e) { return -1; }
    }

    private static <T> void afficherEtChoisir(List<T> liste) {
        for (int i = 0; i < liste.size(); i++) System.out.printf("%d. %s%n", i+1, liste.get(i));
    }

    private static void afficherListe(List<Evenement> liste) {
        if (liste.isEmpty()) { System.out.println("Aucun événement."); return; }
        for (Evenement e : liste) {
            System.out.printf("• %s | %s | ID: %d%n", e.getNom(),
                    e.getDateEvenement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), e.getId());
        }
    }
}