package dao;

import Model.Stade;
import java.util.List;

public interface StadeDAO {
    List<Stade> findAll();
    Stade findById(long id);
    Stade save(Stade stade);
    boolean delete(long id);

    Stade findByNomAndVille(String nom, String ville);
    List<Stade> search(String keyword);
}