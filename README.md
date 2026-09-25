# SportTicket

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-25-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-JDBC-4169E1?logo=postgresql&logoColor=white)
![RMI](https://img.shields.io/badge/Architecture-Java%20RMI-blue)

Application de billetterie pour événements sportifs, construite en **architecture client-serveur** avec **Java RMI**. Les utilisateurs consultent les événements à venir, réservent des places par catégorie, paient et reçoivent des billets électroniques (QR code) par e-mail. Un back-office permet à un administrateur de gérer les événements, les utilisateurs et de suivre le chiffre d'affaires.

## Contexte

Projet académique de type "Client-Serveur" : un serveur RMI expose la logique métier et l'accès aux données, deux clients (une interface graphique JavaFX et un client console) la consomment à distance.

## Fonctionnalités

**Côté client (utilisateur) :**
- Inscription / connexion, gestion du profil
- Consultation des événements à venir et de leurs catégories de places disponibles
- Réservation de places avec expiration automatique (délai limité avant paiement)
- Paiement (simulation carte bancaire) et génération de billets électroniques avec QR code
- Envoi automatique des billets par e-mail
- Consultation des billets déjà achetés

**Côté administration :**
- Création d'événements (sport, stade, ville, capacité, date)
- Ajout de catégories de places (prix, quantité) par événement
- Gestion des utilisateurs (recherche, blocage / déblocage de compte)
- Suivi du chiffre d'affaires total et par événement

## Architecture

```
Client (JavaFX / console)  <--- Java RMI --->  Serveur (registre RMI, port 1099)
                                                     |
                                              Couche service
                                                     |
                                               Couche DAO (JDBC)
                                                     |
                                                PostgreSQL
```

- Le contrat entre client et serveur est défini par l'interface distante `service.TicketService` (`extends Remote`), implémentée côté serveur par `TicketServiceImpl` et invoquée côté client via `java.rmi.Naming.lookup`.
- Le serveur (`ServerMain`) démarre un registre RMI local et y publie le service sous le nom `TicketService`.
- L'accès aux données passe par un DAO par entité (`UtilisateurDAO`, `EvenementDAO`, `ReservationDAO`, `BilletDAO`, ...) au-dessus d'une connexion JDBC PostgreSQL (`dao/DBConnection`).
- L'envoi des billets par e-mail est géré côté serveur par `service/MailSender` (SMTP).
- Les modèles métier (`Utilisateur`, `Evenement`, `Sport`, `Stade`, `Categorie`, `Reservation`, `Paiement`, `Billet`) sont partagés (dupliqués) entre le client et le serveur pour la sérialisation RMI.
- Le dossier [`diagrams/`](diagrams) contient les diagrammes UML du projet (diagramme de classes, cas d'utilisation, diagrammes de séquence pour l'authentification et la réservation).

## Structure du projet

```
billetterie-server/    Serveur RMI : ServerMain, service/, dao/, impl/, Model/
billetterie-client/    Client JavaFX : app/MainApp (GUI), ClientMain (console),
                        presentation/view (FXML), presentation/controller, Model/
DataBase.sql            Script de création du schéma PostgreSQL
diagrams/                Diagrammes UML (classes, cas d'utilisation, séquences)
tools/                   Dépendances externes (driver PostgreSQL, JavaMail, BootstrapFX)
```

## Prérequis

- JDK 25
- [JavaFX SDK 25.0.1](https://openjfx.io/) — le module client référence un dossier `openjfx-25.0.1_linux-x64_bin-sdk` placé au même niveau que `billetterie-client/`
- PostgreSQL

## Installation

1. Créer la base de données PostgreSQL et y exécuter le script [`DataBase.sql`](DataBase.sql) :
   ```bash
   createdb Billetriedb
   psql -d Billetriedb -f DataBase.sql
   ```
2. Vérifier / adapter les identifiants de connexion dans `billetterie-server/src/dao/DBConnection.java` (URL, utilisateur, mot de passe) selon votre installation locale.
3. Les bibliothèques externes nécessaires (driver PostgreSQL, JavaMail, BootstrapFX) sont fournies dans [`tools/`](tools) et déclarées comme dépendances de module dans les projets IntelliJ (`.iml`).

## Lancer le projet

1. **Démarrer le serveur** : exécuter `billetterie-server/src/ServerMain.java` — il initialise le registre RMI sur le port `1099` et publie le service `TicketService`.
2. **Démarrer un client**, une fois le serveur prêt :
   - Interface graphique : `billetterie-client/src/app/MainApp.java` (application JavaFX)
   - Interface console (alternative légère) : `billetterie-client/src/ClientMain.java`

Les deux clients se connectent à `rmi://localhost:1099/TicketService`.
