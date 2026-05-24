// dao/BilletDAO.java
package dao;

import Model.Billet;
import java.util.List;

public interface BilletDAO {
    List<Billet> findByUtilisateurId(long utilisateurId);
    List<Billet> findByPaiementId(long paiementId);
    Billet findById(long id);
    Billet save(Billet billet);
    boolean marquerCommeUtilise(long id);
}