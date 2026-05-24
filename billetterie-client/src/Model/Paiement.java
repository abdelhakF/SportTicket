// ============================================================
// Paiement.java
// ============================================================
package Model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Paiement implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private Long reservationId; // Nullable
    private long utilisateurId;
    private BigDecimal montant;
    private ZonedDateTime datePaiement;
    private String methode;
    private String statut; // PAYE, ECHEC, REMBOURSE
    private String reference;

    // Attributs transients pour les objets liés
    private transient Reservation reservation;
    private transient Utilisateur utilisateur;

    public Paiement() {}

    public Paiement(Long reservationId, long utilisateurId, BigDecimal montant, String methode, String reference) {
        this.reservationId = reservationId;
        this.utilisateurId = utilisateurId;
        this.montant = montant;
        this.methode = methode;
        this.reference = reference;
        this.datePaiement = ZonedDateTime.now();
        this.statut = "PAYE";
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(long utilisateurId) { this.utilisateurId = utilisateurId; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public ZonedDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(ZonedDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getMethode() { return methode; }
    public void setMethode(String methode) { this.methode = methode; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}