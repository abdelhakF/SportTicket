package service;

import Model.Utilisateur;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TicketServiceClient {

    private TicketService service;

    public TicketServiceClient() throws Exception {
        // Connexion au registre RMI sur localhost:1099
        Registry reg = LocateRegistry.getRegistry("localhost", 1099);
        service = (TicketService) reg.lookup("ticketService");
    }

    public Utilisateur inscription(Utilisateur u) throws Exception {
        return service.inscription(u);
    }

    public Utilisateur connexion(String email, String mdp) throws Exception {
        return service.connexion(email, mdp);
    }

    public boolean deconnexion(long id) throws Exception {
        return service.deconnexion(id);
    }
}
