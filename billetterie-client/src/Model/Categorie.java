// ============================================================
// Categorie.java
// ============================================================
package Model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Categorie implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long evenementId;
    private String nom;
    private BigDecimal prix;
    private int placesTotal;
    private int placesReservees;
    private int placesVendues;
    private int placesDisponibles; // Calculé

    // Attribut transient pour l'objet lié
    private transient Evenement evenement;

    public Categorie() {}

    public Categorie(long evenementId, String nom, BigDecimal prix, int placesTotal) {
        this.evenementId = evenementId;
        this.nom = nom;
        this.prix = prix;
        this.placesTotal = placesTotal;
        this.placesReservees = 0;
        this.placesVendues = 0;
        calculerPlacesDisponibles();
    }

    // Méthode pour calculer les places disponibles
    public void calculerPlacesDisponibles() {
        this.placesDisponibles = this.placesTotal - this.placesReservees - this.placesVendues;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getEvenementId() { return evenementId; }
    public void setEvenementId(long evenementId) { this.evenementId = evenementId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public int getPlacesTotal() { return placesTotal; }
    public void setPlacesTotal(int placesTotal) {
        this.placesTotal = placesTotal;
        calculerPlacesDisponibles();
    }

    public int getPlacesReservees() { return placesReservees; }
    public void setPlacesReservees(int placesReservees) {
        this.placesReservees = placesReservees;
        calculerPlacesDisponibles();
    }

    public int getPlacesVendues() { return placesVendues; }
    public void setPlacesVendues(int placesVendues) {
        this.placesVendues = placesVendues;
        calculerPlacesDisponibles();
    }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }
}