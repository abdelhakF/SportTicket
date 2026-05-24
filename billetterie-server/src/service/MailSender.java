package service;

import Model.Billet;
import Model.Utilisateur;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;

public class MailSender {

    // VOS IDENTIFIANTS (Ceux que vous avez fournis)
    private final String username = "othmane.mellouk@etud.iga.ac.ma";
    // ATTENTION : J'ai masqué le mot de passe pour la sécurité ici,
    // remettez votre code à 16 lettres (khgh...) ci-dessous :
    private final String password = "khghcauujflisjvl";

    private final Properties props;

    public MailSender() {
        // Configuration unique dans le constructeur
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        // props.put("mail.debug", "true"); // Décommentez pour voir les logs d'envoi dans la console
    }

    // Méthode privée pour créer la session (évite la duplication de code)
    private Session getSession() {
        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Méthode 1 : Pour envoyer les billets après paiement
     */
    public void envoyerBillets(Utilisateur user, List<Billet> billets, String nomEvenement) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(user.getEmail())
            );
            message.setSubject("Vos billets pour : " + nomEvenement);

            // Construction du corps du mail avec tous les billets
            StringBuilder content = new StringBuilder();
            content.append("Bonjour ").append(user.getPrenom()).append(",\n\n");
            content.append("Félicitations ! Votre commande est validée.\n");
            content.append("Voici vos billets pour l'événement : ").append(nomEvenement).append("\n\n");

            for (Billet b : billets) {
                content.append("------------------------------------------------\n");
                content.append("BILLET N°      : ").append(b.getNumeroBillet()).append("\n");
                content.append("CATÉGORIE      : ").append(b.getNomCategorie() != null ? b.getNomCategorie() : "Standard").append("\n");
                content.append("PRIX           : ").append(b.getPrix()).append(" €\n");
                content.append("CODE DE VALIDATION : ").append(b.getCodeQr()).append("\n");
                content.append("------------------------------------------------\n");
            }

            content.append("\nMerci de votre confiance.\nL'équipe Billetterie Sportive.");

            message.setText(content.toString());

            Transport.send(message);
            System.out.println("✅ Email de billets envoyé à : " + user.getEmail());

        } catch (MessagingException e) {
            System.err.println("❌ Erreur d'envoi mail (Billets) : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode 2 : Pour confirmer l'inscription (adaptation de votre méthode sendNotification)
     */
    public void envoyerConfirmationInscription(Utilisateur user) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(user.getEmail())
            );
            message.setSubject("Bienvenue sur la Billetterie Sportive !");

            String corps = "Bonjour " + user.getNom() + " " + user.getPrenom() + ",\n\n" +
                    "Votre inscription sur la plateforme a été réussie !\n" +
                    "Vous pouvez désormais vous connecter et réserver vos places.\n\n" +
                    "À très vite sur les stades.";

            message.setText(corps);

            Transport.send(message);
            System.out.println("✅ Email d'inscription envoyé à : " + user.getEmail());

        } catch (MessagingException e) {
            System.err.println("❌ Erreur d'envoi mail (Inscription) : " + e.getMessage());
            e.printStackTrace();
        }
    }
}