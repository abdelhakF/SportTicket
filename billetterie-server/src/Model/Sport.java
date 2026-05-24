package Model;

import java.io.Serializable;

public class Sport implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String nom;

    public Sport() {}

    public Sport(String nom) {
        this.nom = nom;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}

