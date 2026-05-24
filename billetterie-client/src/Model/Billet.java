package Model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class Billet implements Serializable {

    // CHANGEMENT RADICAL DU serialVersionUID POUR FORCER LE RECHARGEMENT EN RMI
    private static final long serialVersionUID = 2025120899999999L;

    private long id;
    private long paiementId;
    private long utilisateurId;
    private long categorieId;
    private String numeroBillet;
    private String codeQr;
    private BigDecimal prix;
    private String statut;
    private ZonedDateTime dateEmission;
    private ZonedDateTime dateUtilisation;

    // CHAMPS AJOUTÉS POUR L'AFFICHAGE CÔTÉ CLIENT
    private String nomEvenement;
    private String nomCategorie;

    // Constructeur vide obligatoire pour RMI + JDBC
    public Billet() {
    }

    // Constructeur utilisé lors de la génération des billets après paiement
    public Billet(long paiementId, long utilisateurId, long categorieId, BigDecimal prix) {
        this.paiementId = paiementId;
        this.utilisateurId = utilisateurId;
        this.categorieId = categorieId;
        this.prix = prix;

        this.statut = "VALIDE";
        this.dateEmission = ZonedDateTime.now();

        this.numeroBillet = "BIL-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        this.codeQr = UUID.randomUUID().toString();
    }

    // ====================== GETTERS & SETTERS ======================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPaiementId() { return paiementId; }
    public void setPaiementId(long paiementId) { this.paiementId = paiementId; }

    public long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(long utilisateurId) { this.utilisateurId = utilisateurId; }

    public long getCategorieId() { return categorieId; }
    public void setCategorieId(long categorieId) { this.categorieId = categorieId; }

    public String getNumeroBillet() { return numeroBillet; }
    public void setNumeroBillet(String numeroBillet) { this.numeroBillet = numeroBillet; }

    public String getCodeQr() { return codeQr; }
    public void setCodeQr(String codeQr) { this.codeQr = codeQr; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public ZonedDateTime getDateEmission() { return dateEmission; }
    public void setDateEmission(ZonedDateTime dateEmission) { this.dateEmission = dateEmission; }

    public ZonedDateTime getDateUtilisation() { return dateUtilisation; }
    public void setDateUtilisation(ZonedDateTime dateUtilisation) { this.dateUtilisation = dateUtilisation; }

    // Champs d'affichage
    public String getNomEvenement() { return nomEvenement; }
    public void setNomEvenement(String nomEvenement) { this.nomEvenement = nomEvenement; }

    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }

    // ====================== TOSTRING PROPRE ======================
    @Override
    public String toString() {
        String evt = (nomEvenement != null && !nomEvenement.trim().isEmpty()) ? nomEvenement : "Événement inconnu";
        String cat = (nomCategorie != null && !nomCategorie.trim().isEmpty()) ? nomCategorie : "Catégorie inconnue";
        String qrCourt = (codeQr != null && codeQr.length() > 8) ? codeQr.substring(0, 8) + "..." : codeQr != null ? codeQr : "";

        return String.format("• %-25s | %-18s | %8.2f € | QR: %s", evt, cat, prix != null ? prix : BigDecimal.ZERO, qrCourt);
    }
}