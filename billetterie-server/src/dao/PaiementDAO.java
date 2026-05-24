// dao/PaiementDAO.java
package dao;

import Model.Paiement;
import java.math.BigDecimal;
import java.util.List;

public interface PaiementDAO {
    Paiement findById(long id);
    Paiement save(Paiement paiement);
    List<Paiement> findByUtilisateurId(long utilisateurId);
    BigDecimal getChiffreAffairesTotal();
    List<Object[]> getCAParEvenement();
}