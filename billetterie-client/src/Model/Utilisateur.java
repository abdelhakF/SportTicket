package Model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String role;   // CLIENT, ADMIN
    private String statut; // ACTIF, BLOQUE
    private ZonedDateTime dateInscription;

    public Utilisateur() {}

    // Constructeur simple (pour inscription rapide)
    public Utilisateur(String nom, String prenom, String email, String motDePasse,
                       String telephone, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.role = role;
        this.statut = "ACTIF";
        this.dateInscription = ZonedDateTime.now();
    }

    // Constructeur complet
    public Utilisateur(String nom, String prenom, String email, String motDePasse,
                       String telephone, String role, String statut, ZonedDateTime dateInscription) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.role = role;
        this.statut = statut != null ? statut : "ACTIF";
        this.dateInscription = dateInscription != null ? dateInscription : ZonedDateTime.now();
    }

    // Getters et setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public ZonedDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(ZonedDateTime dateInscription) { this.dateInscription = dateInscription; }
}
