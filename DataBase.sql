-- ==============================================================
-- TABLES (parfaites et sans aucune extension)
-- ==============================================================

CREATE TABLE utilisateur (
    id               BIGSERIAL PRIMARY KEY,
    nom              VARCHAR(100) NOT NULL,
    prenom           VARCHAR(100) NOT NULL,
    email            VARCHAR(150) UNIQUE NOT NULL,
    mot_de_passe     VARCHAR(255) NOT NULL,          -- tu mettras du bcrypt en Java
    telephone        VARCHAR(20),
    role             VARCHAR(10)  NOT NULL DEFAULT 'CLIENT' CHECK (role IN ('CLIENT', 'ADMIN')),
    statut           VARCHAR(10)  NOT NULL DEFAULT 'ACTIF'   CHECK (statut IN ('ACTIF', 'BLOQUE')),
    date_inscription TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE sport (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(80) UNIQUE NOT NULL
);

CREATE TABLE stade (
    id       BIGSERIAL PRIMARY KEY,
    nom      VARCHAR(150) NOT NULL,
    ville    VARCHAR(100) NOT NULL,
    capacite INTEGER NOT NULL CHECK (capacite > 0),
    UNIQUE(nom, ville)
);

CREATE TABLE evenement (
    id             BIGSERIAL PRIMARY KEY,
    nom            VARCHAR(200) NOT NULL,
    date_evenement TIMESTAMPTZ NOT NULL,
    sport_id       BIGINT NOT NULL REFERENCES sport(id),
    stade_id       BIGINT NOT NULL REFERENCES stade(id),
    statut         VARCHAR(15) DEFAULT 'A_VENIR'
                      CHECK (statut IN ('A_VENIR', 'EN_COURS', 'TERMINE', 'ANNULE')),
    UNIQUE(date_evenement, stade_id)
);

CREATE TABLE categorie (
    id                 BIGSERIAL PRIMARY KEY,
    evenement_id       BIGINT NOT NULL REFERENCES evenement(id) ON DELETE CASCADE,
    nom                VARCHAR(100) NOT NULL,
    prix               NUMERIC(10,2) NOT NULL CHECK (prix >= 0),
    places_total       INTEGER NOT NULL CHECK (places_total > 0),
    places_reservees   INTEGER DEFAULT 0 CHECK (places_reservees >= 0),
    places_vendues     INTEGER DEFAULT 0 CHECK (places_vendues >= 0),
    
    -- COLONNE CALCULÉE CORRIGÉE POUR POSTGRESQL
    places_disponibles INTEGER GENERATED ALWAYS AS 
        (places_total - places_reservees - places_vendues) STORED,

    CONSTRAINT chk_stock CHECK (places_reservees + places_vendues <= places_total),
    UNIQUE(evenement_id, nom)
);

CREATE TABLE reservation (
    id               BIGSERIAL PRIMARY KEY,
    utilisateur_id   BIGINT NOT NULL REFERENCES utilisateur(id),
    categorie_id     BIGINT NOT NULL REFERENCES categorie(id) ON DELETE CASCADE,
    quantite         INTEGER NOT NULL CHECK (quantite > 0),
    prix_unitaire    NUMERIC(10,2) NOT NULL,
    date_creation    TIMESTAMPTZ DEFAULT NOW(),
    date_expiration  TIMESTAMPTZ NOT NULL, -- tu rempliras + 15 min en Java
    statut           VARCHAR(15) DEFAULT 'EN_ATTENTE'
                        CHECK (statut IN ('EN_ATTENTE', 'PAYEE', 'EXPIREE', 'ANNULEE'))
);

CREATE INDEX idx_reservation_exp ON reservation(date_expiration) WHERE statut = 'EN_ATTENTE';

CREATE TABLE paiement (
    id              BIGSERIAL PRIMARY KEY,
    reservation_id  BIGINT REFERENCES reservation(id),
    utilisateur_id  BIGINT NOT NULL REFERENCES utilisateur(id),
    montant         NUMERIC(12,2) NOT NULL,
    date_paiement   TIMESTAMPTZ DEFAULT NOW(),
    methode         VARCHAR(30) NOT NULL,
    statut          VARCHAR(15) DEFAULT 'PAYE' CHECK (statut IN ('PAYE', 'ECHEC', 'REMBOURSE')),
    reference       VARCHAR(100) UNIQUE
);

CREATE TABLE billet (
    id               BIGSERIAL PRIMARY KEY,
    paiement_id      BIGINT NOT NULL REFERENCES paiement(id),
    utilisateur_id   BIGINT NOT NULL REFERENCES utilisateur(id),
    categorie_id     BIGINT NOT NULL REFERENCES categorie(id),
    numero_billet    VARCHAR(40) UNIQUE NOT NULL, -- tu généreras en Java
    code_qr          VARCHAR(255) UNIQUE NOT NULL, -- tu généreras en Java (UUID.randomUUID())
    prix             NUMERIC(10,2) NOT NULL,
    statut           VARCHAR(15) DEFAULT 'VALIDE' CHECK (statut IN ('VALIDE', 'UTILISE', 'ANNULE')),
    date_emission    TIMESTAMPTZ DEFAULT NOW(),
    date_utilisation TIMESTAMPTZ
);
