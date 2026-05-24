package Model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long utilisateurId;
    private long categorieId;
    private long evenementId; // Utile si présent dans votre table
    private int quantite;
    private BigDecimal prixTotal;
    private BigDecimal prixUnitaire; // Ajouté pour cohérence
    private String statut;
    private ZonedDateTime dateCreation;
    private ZonedDateTime dateExpiration;

    // --- NOUVEAUX CHAMPS (Pour l'affichage dans le panier) ---
    private String nomEvenement;
    private String nomCategorie;

    public Reservation() {}

    // Constructeur complet
    public Reservation(long utilisateurId, long categorieId, int quantite, BigDecimal prixUnitaire) {
        this.utilisateurId = utilisateurId;
        this.categorieId = categorieId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.prixTotal = prixUnitaire.multiply(new BigDecimal(quantite));
        this.statut = "EN_ATTENTE";
        this.dateCreation = ZonedDateTime.now();
    }

    // --- GETTERS & SETTERS POUR L'AFFICHAGE ---
    public String getNomEvenement() { return nomEvenement; }
    public void setNomEvenement(String nomEvenement) { this.nomEvenement = nomEvenement; }

    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }
    // ------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(long utilisateurId) { this.utilisateurId = utilisateurId; }

    public long getCategorieId() { return categorieId; }
    public void setCategorieId(long categorieId) { this.categorieId = categorieId; }

    public long getEvenementId() { return evenementId; }
    public void setEvenementId(long evenementId) { this.evenementId = evenementId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixTotal() { return prixTotal; }
    public void setPrixTotal(BigDecimal prixTotal) { this.prixTotal = prixTotal; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public ZonedDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(ZonedDateTime dateCreation) { this.dateCreation = dateCreation; }

    public ZonedDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(ZonedDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    @Override
    public String toString() {
        // Affichage console propre pour le client
        String evt = (nomEvenement != null) ? nomEvenement : "Événement inconnu";
        String cat = (nomCategorie != null) ? nomCategorie : "Catégorie inconnue";

        long minutesRestantes = 0;
        if (dateExpiration != null) {
            long now = System.currentTimeMillis();
            long exp = dateExpiration.toInstant().toEpochMilli();
            minutesRestantes = (exp - now) / 60000;
        }

        return String.format("%s - %s × %d → %.2f € (expire dans %d min)",
                evt, cat, quantite, prixTotal, Math.max(0, minutesRestantes));
    }
}