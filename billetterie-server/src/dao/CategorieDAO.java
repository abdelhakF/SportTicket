// dao/CategorieDAO.java
package dao;

import Model.Categorie;
import java.util.List;

public interface CategorieDAO {
    List<Categorie> findByEvenementId(long evenementId);
    Categorie findById(long id);
    Categorie save(Categorie categorie);
    boolean updatePlaces(long id, int reserveesToAdd, int venduesToAdd);
    boolean delete(long id);
}