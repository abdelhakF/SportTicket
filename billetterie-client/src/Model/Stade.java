// ============================================================
// Stade.java
// ============================================================
package Model;

import java.io.Serializable;

public class Stade implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String nom;
    private String ville;
    private int capacite;

    public Stade() {}

    public Stade(String nom, String ville, int capacite) {
        this.nom = nom;
        this.ville = ville;
        this.capacite = capacite;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
}
