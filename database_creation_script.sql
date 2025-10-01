-- ===============================================
-- SCRIPT SQL - SYSTÈME DE MICROFINANCE SCORING
-- Base de données: microfinance_scoring
-- Date: Octobre 2025
-- ===============================================

-- Création de la base de données
DROP DATABASE IF EXISTS microfinance_scoring;
CREATE DATABASE microfinance_scoring CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE microfinance_scoring;

-- ===============================================
-- TABLE PRINCIPALE: PERSONNES
-- ===============================================
CREATE TABLE personnes (
    id VARCHAR(36) PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    ville VARCHAR(100) NOT NULL,
    nombre_enfants INT DEFAULT 0,
    investissement TEXT,
    placement TEXT,
    situation_familiale ENUM('CELIBATAIRE', 'MARIE', 'DIVORCE', 'VEUF') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    score INT DEFAULT 0,
    type_client ENUM('EMPLOYE', 'PROFESSIONNEL') NOT NULL,
    
    INDEX idx_type_client (type_client),
    INDEX idx_score (score),
    INDEX idx_situation_familiale (situation_familiale),
    INDEX idx_created_at (created_at)
);

-- ===============================================
-- TABLE: EMPLOYES (Hérite de PERSONNES)
-- ===============================================
CREATE TABLE employes (
    personne_id VARCHAR(36) PRIMARY KEY,
    salaire DECIMAL(10,2) NOT NULL,
    anciennete INT NOT NULL COMMENT 'Ancienneté en mois',
    poste VARCHAR(100) NOT NULL,
    type_contrat ENUM('CDI', 'CDD', 'STAGE', 'FREELANCE', 'TEMPORAIRE') NOT NULL,
    secteur ENUM('PRIVE', 'PUBLIC', 'ASSOCIATIF', 'INTERNATIONAL') NOT NULL,
    
    FOREIGN KEY (personne_id) REFERENCES personnes(id) ON DELETE CASCADE,
    INDEX idx_salaire (salaire),
    INDEX idx_type_contrat (type_contrat),
    INDEX idx_secteur (secteur),
    INDEX idx_anciennete (anciennete)
);

-- ===============================================
-- TABLE: PROFESSIONNELS (Hérite de PERSONNES)
-- ===============================================
CREATE TABLE professionnels (
    personne_id VARCHAR(36) PRIMARY KEY,
    revenu DECIMAL(10,2) NOT NULL,
    immatriculation_fiscale VARCHAR(50),
    secteur_activite ENUM('AGRICULTURE', 'ARTISANAT', 'COMMERCE', 'SERVICE', 'INDUSTRIE', 'TRANSPORT', 'SANTE', 'EDUCATION', 'TECHNOLOGIE', 'AUTRE') NOT NULL,
    activite VARCHAR(200) NOT NULL,
    
    FOREIGN KEY (personne_id) REFERENCES personnes(id) ON DELETE CASCADE,
    INDEX idx_revenu (revenu),
    INDEX idx_secteur_activite (secteur_activite),
    INDEX idx_immatriculation (immatriculation_fiscale)
);

-- ===============================================
-- TABLE: CREDITS
-- ===============================================
CREATE TABLE credits (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL,
    montant_demande DECIMAL(10,2) NOT NULL,
    montant_octroye DECIMAL(10,2),
    duree_mois INT NOT NULL,
    taux_interet DECIMAL(5,2) NOT NULL,
    type_credit ENUM('PERSONNEL', 'IMMOBILIER', 'AUTO', 'PROFESSIONNEL', 'ETUDIANT', 'URGENCE') NOT NULL,
    decision ENUM('EN_ATTENTE', 'ACCORD_IMMEDIAT', 'ETUDE_APPROFONDIE', 'REFUS') DEFAULT 'EN_ATTENTE',
    score_client INT NOT NULL,
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_decision TIMESTAMP NULL,
    date_debut DATE,
    date_fin DATE,
    statut ENUM('ACTIF', 'TERMINE', 'SUSPENDU', 'ANNULE') DEFAULT 'ACTIF',
    observations TEXT,
    
    FOREIGN KEY (client_id) REFERENCES personnes(id) ON DELETE CASCADE,
    INDEX idx_client_id (client_id),
    INDEX idx_decision (decision),
    INDEX idx_type_credit (type_credit),
    INDEX idx_statut (statut),
    INDEX idx_date_demande (date_demande),
    INDEX idx_montant_demande (montant_demande)
);

-- ===============================================
-- TABLE: ECHEANCES
-- ===============================================
CREATE TABLE echeances (
    id VARCHAR(36) PRIMARY KEY,
    credit_id VARCHAR(36) NOT NULL,
    numero_echeance INT NOT NULL,
    date_echeance DATE NOT NULL,
    montant_echeance DECIMAL(10,2) NOT NULL,
    montant_capital DECIMAL(10,2) NOT NULL,
    montant_interet DECIMAL(10,2) NOT NULL,
    date_paiement DATE NULL,
    montant_paye DECIMAL(10,2) DEFAULT 0,
    statut_paiement ENUM('EN_ATTENTE', 'PAYE', 'RETARD', 'PARTIEL', 'ANNULE') DEFAULT 'EN_ATTENTE',
    jours_retard INT DEFAULT 0,
    
    FOREIGN KEY (credit_id) REFERENCES credits(id) ON DELETE CASCADE,
    INDEX idx_credit_id (credit_id),
    INDEX idx_date_echeance (date_echeance),
    INDEX idx_statut_paiement (statut_paiement),
    INDEX idx_numero_echeance (numero_echeance),
    INDEX idx_jours_retard (jours_retard),
    
    UNIQUE KEY unique_credit_numero (credit_id, numero_echeance)
);

-- ===============================================
-- TABLE: INCIDENTS
-- ===============================================
CREATE TABLE incidents (
    id VARCHAR(36) PRIMARY KEY,
    echeance_id VARCHAR(36) NOT NULL,
    date_incident DATE NOT NULL,
    type_incident ENUM('RETARD_PAIEMENT', 'PAIEMENT_PARTIEL', 'CHEQUE_REJETE', 'DEFAUT_PAIEMENT', 'FRAUDE', 'AUTRE') NOT NULL,
    score_impact INT NOT NULL COMMENT 'Impact sur le score (-50 à -5)',
    description TEXT,
    statut ENUM('OUVERT', 'RESOLU', 'EN_COURS', 'ABANDONNE') DEFAULT 'OUVERT',
    date_resolution DATE NULL,
    
    FOREIGN KEY (echeance_id) REFERENCES echeances(id) ON DELETE CASCADE,
    INDEX idx_echeance_id (echeance_id),
    INDEX idx_date_incident (date_incident),
    INDEX idx_type_incident (type_incident),
    INDEX idx_statut (statut),
    INDEX idx_score_impact (score_impact)
);

-- ===============================================
-- TABLE: HISTORIQUE_SCORES
-- ===============================================
CREATE TABLE historique_scores (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL,
    ancien_score INT NOT NULL,
    nouveau_score INT NOT NULL,
    variation INT GENERATED ALWAYS AS (nouveau_score - ancien_score) STORED,
    motif VARCHAR(200) NOT NULL,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    credit_id VARCHAR(36) NULL,
    incident_id VARCHAR(36) NULL,
    
    FOREIGN KEY (client_id) REFERENCES personnes(id) ON DELETE CASCADE,
    FOREIGN KEY (credit_id) REFERENCES credits(id) ON DELETE SET NULL,
    FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE SET NULL,
    INDEX idx_client_id (client_id),
    INDEX idx_date_modification (date_modification),
    INDEX idx_variation (variation)
);

-- ===============================================
-- TABLE: PARAMETRES_SCORING
-- ===============================================
CREATE TABLE parametres_scoring (
    id INT AUTO_INCREMENT PRIMARY KEY,
    composant ENUM('STABILITE_PROFESSIONNELLE', 'CAPACITE_FINANCIERE', 'HISTORIQUE_CREDIT', 'RELATION_CLIENT', 'CRITERES_COMPLEMENTAIRES') NOT NULL,
    sous_composant VARCHAR(100) NOT NULL,
    poids DECIMAL(5,2) NOT NULL COMMENT 'Poids en pourcentage',
    seuil_min INT DEFAULT 0,
    seuil_max INT DEFAULT 100,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_composant_sous (composant, sous_composant),
    INDEX idx_composant (composant),
    INDEX idx_actif (actif)
);

-- ===============================================
-- INSERTION DES PARAMÈTRES DE SCORING
-- ===============================================
INSERT INTO parametres_scoring (composant, sous_composant, poids, seuil_min, seuil_max) VALUES
-- Stabilité Professionnelle (25%)
('STABILITE_PROFESSIONNELLE', 'anciennete_emploi', 10.00, 0, 100),
('STABILITE_PROFESSIONNELLE', 'type_contrat', 8.00, 0, 100),
('STABILITE_PROFESSIONNELLE', 'secteur_activite', 7.00, 0, 100),

-- Capacité Financière (30%)
('CAPACITE_FINANCIERE', 'revenus_mensuels', 15.00, 0, 100),
('CAPACITE_FINANCIERE', 'ratio_endettement', 10.00, 0, 100),
('CAPACITE_FINANCIERE', 'capacite_epargne', 5.00, 0, 100),

-- Historique Crédit (20%)
('HISTORIQUE_CREDIT', 'credits_anterieurs', 8.00, 0, 100),
('HISTORIQUE_CREDIT', 'incidents_paiement', 7.00, 0, 100),
('HISTORIQUE_CREDIT', 'ponctualite_remboursement', 5.00, 0, 100),

-- Relation Client (15%)
('RELATION_CLIENT', 'anciennete_relation', 6.00, 0, 100),
('RELATION_CLIENT', 'engagement_client', 5.00, 0, 100),
('RELATION_CLIENT', 'communication', 4.00, 0, 100),

-- Critères Complémentaires (10%)
('CRITERES_COMPLEMENTAIRES', 'situation_familiale', 4.00, 0, 100),
('CRITERES_COMPLEMENTAIRES', 'patrimoine', 3.00, 0, 100),
('CRITERES_COMPLEMENTAIRES', 'garanties', 3.00, 0, 100);

-- ===============================================
-- VUES UTILES
-- ===============================================

-- Vue: Clients avec leurs informations complètes
CREATE VIEW v_clients_complets AS
SELECT 
    p.id,
    p.nom,
    p.prenom,
    p.date_naissance,
    YEAR(CURDATE()) - YEAR(p.date_naissance) as age,
    p.ville,
    p.nombre_enfants,
    p.situation_familiale,
    p.score,
    p.type_client,
    p.created_at,
    -- Informations spécifiques employés
    e.salaire as employe_salaire,
    e.anciennete as employe_anciennete,
    e.poste as employe_poste,
    e.type_contrat as employe_contrat,
    e.secteur as employe_secteur,
    -- Informations spécifiques professionnels
    pr.revenu as professionnel_revenu,
    pr.immatriculation_fiscale as professionnel_immatriculation,
    pr.secteur_activite as professionnel_secteur,
    pr.activite as professionnel_activite
FROM personnes p
LEFT JOIN employes e ON p.id = e.personne_id
LEFT JOIN professionnels pr ON p.id = pr.personne_id;

-- Vue: Statistiques des crédits
CREATE VIEW v_statistiques_credits AS
SELECT 
    type_credit,
    decision,
    COUNT(*) as nombre_credits,
    AVG(montant_demande) as montant_moyen_demande,
    AVG(montant_octroye) as montant_moyen_octroye,
    AVG(score_client) as score_moyen_client,
    AVG(taux_interet) as taux_moyen,
    MIN(date_demande) as premiere_demande,
    MAX(date_demande) as derniere_demande
FROM credits
GROUP BY type_credit, decision;

-- Vue: Performance des remboursements
CREATE VIEW v_performance_remboursements AS
SELECT 
    c.id as credit_id,
    c.client_id,
    p.nom,
    p.prenom,
    c.montant_octroye,
    COUNT(e.id) as nb_echeances_total,
    COUNT(CASE WHEN e.statut_paiement = 'PAYE' THEN 1 END) as nb_echeances_payees,
    COUNT(CASE WHEN e.statut_paiement = 'RETARD' THEN 1 END) as nb_echeances_retard,
    ROUND(COUNT(CASE WHEN e.statut_paiement = 'PAYE' THEN 1 END) * 100.0 / COUNT(e.id), 2) as taux_ponctualite,
    AVG(e.jours_retard) as moyenne_jours_retard,
    SUM(e.montant_paye) as total_paye,
    SUM(e.montant_echeance) as total_du
FROM credits c
JOIN personnes p ON c.client_id = p.id
LEFT JOIN echeances e ON c.id = e.credit_id
WHERE c.statut = 'ACTIF'
GROUP BY c.id, c.client_id, p.nom, p.prenom, c.montant_octroye;

-- ===============================================
-- PROCÉDURES STOCKÉES UTILES
-- ===============================================

DELIMITER //

-- Procédure: Calculer le score d'un client
CREATE PROCEDURE sp_calculer_score_client(IN client_id VARCHAR(36), OUT nouveau_score INT)
BEGIN
    DECLARE score_stabilite INT DEFAULT 0;
    DECLARE score_financier INT DEFAULT 0;
    DECLARE score_historique INT DEFAULT 0;
    DECLARE score_relation INT DEFAULT 0;
    DECLARE score_complementaire INT DEFAULT 0;
    
    -- Calculer score de stabilité (exemple simplifié)
    SELECT CASE 
        WHEN p.type_client = 'EMPLOYE' THEN
            CASE 
                WHEN e.anciennete >= 24 AND e.type_contrat = 'CDI' THEN 25
                WHEN e.anciennete >= 12 AND e.type_contrat = 'CDI' THEN 20
                WHEN e.anciennete >= 6 THEN 15
                ELSE 10
            END
        ELSE
            CASE 
                WHEN pr.immatriculation_fiscale IS NOT NULL AND pr.revenu >= 5000 THEN 20
                WHEN pr.revenu >= 3000 THEN 15
                ELSE 10
            END
    END INTO score_stabilite
    FROM personnes p
    LEFT JOIN employes e ON p.id = e.personne_id
    LEFT JOIN professionnels pr ON p.id = pr.personne_id
    WHERE p.id = client_id;
    
    -- Score financier basé sur les revenus
    SELECT CASE 
        WHEN p.type_client = 'EMPLOYE' THEN
            CASE 
                WHEN e.salaire >= 8000 THEN 30
                WHEN e.salaire >= 5000 THEN 25
                WHEN e.salaire >= 3000 THEN 20
                ELSE 15
            END
        ELSE
            CASE 
                WHEN pr.revenu >= 10000 THEN 30
                WHEN pr.revenu >= 6000 THEN 25
                WHEN pr.revenu >= 3000 THEN 20
                ELSE 15
            END
    END INTO score_financier
    FROM personnes p
    LEFT JOIN employes e ON p.id = e.personne_id
    LEFT JOIN professionnels pr ON p.id = pr.personne_id
    WHERE p.id = client_id;
    
    -- Score historique (basé sur les incidents)
    SELECT CASE 
        WHEN COUNT(i.id) = 0 THEN 20
        WHEN COUNT(i.id) <= 2 THEN 15
        WHEN COUNT(i.id) <= 5 THEN 10
        ELSE 5
    END INTO score_historique
    FROM personnes p
    LEFT JOIN credits c ON p.id = c.client_id
    LEFT JOIN echeances e ON c.id = e.credit_id
    LEFT JOIN incidents i ON e.id = i.echeance_id
    WHERE p.id = client_id AND i.date_incident >= DATE_SUB(CURDATE(), INTERVAL 2 YEAR);
    
    -- Score relation (ancienneté client)
    SELECT CASE 
        WHEN DATEDIFF(CURDATE(), p.created_at) >= 730 THEN 15
        WHEN DATEDIFF(CURDATE(), p.created_at) >= 365 THEN 12
        WHEN DATEDIFF(CURDATE(), p.created_at) >= 180 THEN 8
        ELSE 5
    END INTO score_relation
    FROM personnes p
    WHERE p.id = client_id;
    
    -- Score complémentaire (situation familiale, enfants)
    SELECT CASE 
        WHEN p.situation_familiale = 'MARIE' AND p.nombre_enfants BETWEEN 1 AND 3 THEN 10
        WHEN p.situation_familiale = 'MARIE' THEN 8
        WHEN p.nombre_enfants <= 2 THEN 6
        ELSE 4
    END INTO score_complementaire
    FROM personnes p
    WHERE p.id = client_id;
    
    SET nouveau_score = score_stabilite + score_financier + score_historique + score_relation + score_complementaire;
    
    -- Mettre à jour le score dans la table
    UPDATE personnes SET score = nouveau_score WHERE id = client_id;
    
END //

-- Procédure: Génération des échéances pour un crédit
CREATE PROCEDURE sp_generer_echeances(IN credit_id VARCHAR(36))
BEGIN
    DECLARE montant_credit DECIMAL(10,2);
    DECLARE duree_mois INT;
    DECLARE taux_mensuel DECIMAL(10,6);
    DECLARE montant_mensuel DECIMAL(10,2);
    DECLARE date_debut DATE;
    DECLARE i INT DEFAULT 1;
    DECLARE date_echeance DATE;
    DECLARE montant_interet DECIMAL(10,2);
    DECLARE montant_capital DECIMAL(10,2);
    DECLARE capital_restant DECIMAL(10,2);
    
    -- Récupérer les informations du crédit
    SELECT montant_octroye, duree_mois, taux_interet/100/12, date_debut
    INTO montant_credit, duree_mois, taux_mensuel, date_debut
    FROM credits WHERE id = credit_id;
    
    SET capital_restant = montant_credit;
    SET montant_mensuel = montant_credit * (taux_mensuel * POWER(1 + taux_mensuel, duree_mois)) / (POWER(1 + taux_mensuel, duree_mois) - 1);
    
    -- Supprimer les anciennes échéances si elles existent
    DELETE FROM echeances WHERE credit_id = credit_id;
    
    -- Générer les échéances
    WHILE i <= duree_mois DO
        SET date_echeance = DATE_ADD(date_debut, INTERVAL i MONTH);
        SET montant_interet = capital_restant * taux_mensuel;
        SET montant_capital = montant_mensuel - montant_interet;
        
        -- Ajustement pour la dernière échéance
        IF i = duree_mois THEN
            SET montant_capital = capital_restant;
            SET montant_mensuel = montant_capital + montant_interet;
        END IF;
        
        INSERT INTO echeances (id, credit_id, numero_echeance, date_echeance, montant_echeance, montant_capital, montant_interet)
        VALUES (UUID(), credit_id, i, date_echeance, montant_mensuel, montant_capital, montant_interet);
        
        SET capital_restant = capital_restant - montant_capital;
        SET i = i + 1;
    END WHILE;
    
END //

DELIMITER ;

-- ===============================================
-- TRIGGERS
-- ===============================================

DELIMITER //

-- Trigger: Mise à jour automatique du score après incident
CREATE TRIGGER tr_incident_score_update
AFTER INSERT ON incidents
FOR EACH ROW
BEGIN
    DECLARE client_id VARCHAR(36);
    DECLARE nouveau_score INT;
    
    -- Récupérer l'ID du client
    SELECT c.client_id INTO client_id
    FROM echeances e
    JOIN credits c ON e.credit_id = c.id
    WHERE e.id = NEW.echeance_id;
    
    -- Calculer le nouveau score
    CALL sp_calculer_score_client(client_id, nouveau_score);
    
    -- Enregistrer dans l'historique
    INSERT INTO historique_scores (id, client_id, ancien_score, nouveau_score, motif, incident_id)
    SELECT UUID(), client_id, score, nouveau_score, CONCAT('Incident: ', NEW.type_incident), NEW.id
    FROM personnes WHERE id = client_id;
    
END //

-- Trigger: Calcul automatique des jours de retard
CREATE TRIGGER tr_echeance_retard_update
BEFORE UPDATE ON echeances
FOR EACH ROW
BEGIN
    IF NEW.date_paiement IS NOT NULL AND NEW.date_paiement > NEW.date_echeance THEN
        SET NEW.jours_retard = DATEDIFF(NEW.date_paiement, NEW.date_echeance);
        SET NEW.statut_paiement = CASE 
            WHEN NEW.montant_paye >= NEW.montant_echeance THEN 'PAYE'
            WHEN NEW.montant_paye > 0 THEN 'PARTIEL'
            ELSE 'RETARD'
        END;
    ELSEIF NEW.date_paiement IS NOT NULL THEN
        SET NEW.jours_retard = 0;
        SET NEW.statut_paiement = CASE 
            WHEN NEW.montant_paye >= NEW.montant_echeance THEN 'PAYE'
            WHEN NEW.montant_paye > 0 THEN 'PARTIEL'
            ELSE NEW.statut_paiement
        END;
    END IF;
END //

DELIMITER ;

-- ===============================================
-- INDEX POUR OPTIMISATION DES PERFORMANCES
-- ===============================================

-- Index composites pour les requêtes fréquentes
CREATE INDEX idx_credits_client_decision ON credits(client_id, decision);
CREATE INDEX idx_echeances_credit_statut ON echeances(credit_id, statut_paiement);
CREATE INDEX idx_incidents_date_type ON incidents(date_incident, type_incident);
CREATE INDEX idx_personnes_score_type ON personnes(score DESC, type_client);

-- ===============================================
-- DONNÉES DE TEST (OPTIONNEL)
-- ===============================================

-- Quelques clients de test
INSERT INTO personnes (id, nom, prenom, date_naissance, ville, nombre_enfants, situation_familiale, score, type_client) VALUES
(UUID(), 'ALAMI', 'Ahmed', '1985-03-15', 'Casablanca', 2, 'MARIE', 75, 'EMPLOYE'),
(UUID(), 'BENALI', 'Fatima', '1990-07-22', 'Rabat', 1, 'MARIE', 82, 'PROFESSIONNEL'),
(UUID(), 'CHERKAOUI', 'Mohamed', '1988-11-08', 'Fès', 0, 'CELIBATAIRE', 68, 'EMPLOYE');

-- ===============================================
-- FIN DU SCRIPT
-- ===============================================

-- Affichage des statistiques
SELECT 'Base de données créée avec succès!' as message;
SELECT COUNT(*) as nb_tables FROM information_schema.tables WHERE table_schema = 'microfinance_scoring';
SELECT COUNT(*) as nb_parametres_scoring FROM parametres_scoring;

-- Vérification des contraintes
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM information_schema.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'microfinance_scoring'
ORDER BY TABLE_NAME, CONSTRAINT_TYPE;