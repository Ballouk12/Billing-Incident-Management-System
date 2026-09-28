-- Fichier: src/main/resources/data.sql

-- Insertion des utilisateurs par défaut
-- Mot de passe hashé pour "admin123", "super123", "tech123" avec BCrypt

INSERT INTO users (username, email, password, first_name, last_name, role, active, created_at, updated_at)
VALUES
    ('admin', 'admin@billing.com', '$2a$10$xQN3.1YxDYxO/DKf1YHsNOqV7xTGHXD3kE8n2sMFWx4WEb8bQvQKS', 'Admin', 'System', 'ADMIN', true, NOW(), NOW()),
    ('superviseur1', 'super1@billing.com', '$2a$10$xQN3.1YxDYxO/DKf1YHsNOqV7xTGHXD3kE8n2sMFWx4WEb8bQvQKS', 'Jean', 'Dupont', 'SUPERVISEUR', true, NOW(), NOW()),
    ('tech1', 'tech1@billing.com', '$2a$10$xQN3.1YxDYxO/DKf1YHsNOqV7xTGHXD3kE8n2sMFWx4WEb8bQvQKS', 'Marie', 'Martin', 'TECHNICIEN', true, NOW(), NOW()),
    ('tech2', 'tech2@billing.com', '$2a$10$xQN3.1YxDYxO/DKf1YHsNOqV7xTGHXD3kE8n2sMFWx4WEb8bQvQKS', 'Paul', 'Bernard', 'TECHNICIEN', true, NOW(), NOW())
    ON DUPLICATE KEY UPDATE username=username;

-- Insertion d'incidents de test
INSERT INTO incidents (incident_type, status, description, reference_facture, client_id, montant_erreur, source_file, line_number, priority, created_by_id, created_at, updated_at, deleted)
VALUES
    ('DOUBLON', 'NOUVEAU', 'Facture en double détectée lors du traitement', 'FAC2025001', 'CLI123', 2500.00, 'rollback_20250101.csv', 42, 'MOYENNE', 1, NOW(), NOW(), false),
    ('ERREUR_CALCUL', 'EN_COURS', 'Erreur de calcul de TVA sur facture', 'FAC2025002', 'CLI456', 850.50, 'rollback_20250101.csv', 105, 'HAUTE', 1, NOW(), NOW(), false),
    ('LIGNE_INCOMPLETE', 'NOUVEAU', 'Ligne de facturation incomplète - champ montant manquant', 'FAC2025003', 'CLI789', 0.00, 'rollback_20250102.csv', 87, 'FAIBLE', 1, NOW(), NOW(), false),
    ('MONTANT_INCORRECT', 'RESOLU', 'Montant facturé ne correspond pas au tarif', 'FAC2025004', 'CLI321', 15000.00, 'rollback_20250102.csv', 203, 'CRITIQUE', 1, NOW(), NOW(), false),
    ('REFERENCE_INVALIDE', 'NOUVEAU', 'Référence de facture invalide dans le système', 'FAC2025005', 'CLI654', 500.00, 'rollback_20250103.csv', 45, 'MOYENNE', 1, NOW(), NOW(), false)
    ON DUPLICATE KEY UPDATE id=id;

-- Assigner quelques incidents aux techniciens
UPDATE incidents SET assigned_to_id = 3, status = 'EN_COURS' WHERE id = 2;
UPDATE incidents SET assigned_to_id = 4, status = 'RESOLU', resolved_at = NOW() WHERE id = 4;

-- Insertion de solutions pour incidents
INSERT INTO solutions (incident_id, created_by_id, description, solution_type, validated, created_at, comments)
VALUES
    (2, 3, 'Recalcul de la TVA effectué. Le taux de 20% a été appliqué correctement.', 'CORRECTION', true, NOW(), 'Solution validée par le superviseur'),
    (4, 4, 'Ajustement du montant selon la grille tarifaire en vigueur. Client contacté et informé.', 'PERMANENT', true, NOW(), 'Facture rectificative émise')
    ON DUPLICATE KEY UPDATE id=id;

-- Insertion de logs d'audit
INSERT INTO audit_logs (incident_id, user_id, action, details, created_at, ip_address)
VALUES
    (1, 1, 'CREATED', 'Incident créé automatiquement depuis fichier rollback', NOW(), '127.0.0.1'),
    (2, 1, 'CREATED', 'Incident créé automatiquement depuis fichier rollback', NOW(), '127.0.0.1'),
    (2, 2, 'ASSIGNED', 'Incident assigné au technicien tech1', NOW(), '192.168.1.100'),
    (2, 3, 'SOLUTION_ADDED', 'Solution proposée par le technicien', NOW(), '192.168.1.105'),
    (4, 4, 'RESOLVED', 'Incident marqué comme résolu', NOW(), '192.168.1.106')
    ON DUPLICATE KEY UPDATE id=id;