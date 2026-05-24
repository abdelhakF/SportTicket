package Model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Evenement implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String nom;
    private ZonedDateTime dateEvenement;
    private long sportId;
    private long stadeId;
    private String statut; // A_VENIR, EN_COURS, TERMINE, ANNULE

    // Attributs transients pour les objets liés (non sérialisés)
    private transient Sport sport;
    private transient Stade stade;

    public Evenement() {}

    public Evenement(String nom, ZonedDateTime dateEvenement, long sportId, long stadeId) {
        this.nom = nom;
        this.dateEvenement = dateEvenement;
        this.sportId = sportId;
        this.stadeId = stadeId;
        this.statut = "A_VENIR";
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public ZonedDateTime getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(ZonedDateTime dateEvenement) { this.dateEvenement = dateEvenement; }

    public long getSportId() { return sportId; }
    public void setSportId(long sportId) { this.sportId = sportId; }

    public long getStadeId() { return stadeId; }
    public void setStadeId(long stadeId) { this.stadeId = stadeId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public Stade getStade() { return stade; }
    public void setStade(Stade stade) { this.stade = stade; }
}
