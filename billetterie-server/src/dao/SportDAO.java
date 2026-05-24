package dao;

import Model.Sport;
import java.util.List;

public interface SportDAO {
    List<Sport> findAll();
    Sport findById(long id);
    Sport save(Sport sport);
    boolean delete(long id);

    // RECHERCHE PAR NOM
    Sport findByNom(String nom);
    List<Sport> searchByNom(String keyword);
}