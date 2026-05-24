package dao;

import Model.Evenement;
import java.time.ZonedDateTime;
import java.util.List;

public interface EvenementDAO {
    List<Evenement> findAll();
    List<Evenement> findAVenir();
    Evenement findById(long id);
    Evenement save(Evenement evenement);
    boolean updateStatut(long id, String statut);
    boolean delete(long id);

    List<Evenement> search(String keyword);
}