Feature: Extraction des incidents depuis fichiers Rollback
  En tant qu'administrateur système
  Je veux extraire automatiquement les incidents des fichiers rollback
  Afin de centraliser et traiter efficacement les anomalies de facturation

  Background:
    Given un utilisateur authentifié avec le rôle "ADMIN"
    And la base de données est initialisée

  Scenario: Extraction réussie d'un fichier rollback valide
    Given un fichier rollback "rollback_2025_01.csv" est disponible
    And le fichier contient 10 lignes d'incidents valides
    When le service d'extraction est exécuté pour ce fichier
    Then 10 incidents sont créés dans la base de données
    And chaque incident a un statut "NOUVEAU"
    And un message de succès est retourné
    And les incidents sont indexés dans Elasticsearch

  Scenario: Extraction avec fichier contenant des doublons
    Given un fichier rollback "rollback_doublons.csv" est disponible
    And le fichier contient 5 lignes avec des incidents doublons
    When le service d'extraction est exécuté pour ce fichier
    Then les doublons sont identifiés et marqués comme type "DOUBLON"
    And 5 incidents sont créés avec le type approprié
    And un log d'audit est créé pour chaque incident

  Scenario: Échec d'extraction avec fichier invalide
    Given un fichier "invalid_file.txt" avec un format incorrect
    When le service d'extraction est exécuté pour ce fichier
    Then une exception "InvalidFileException" est levée
    And un message d'erreur explicite est retourné
    And aucun incident n'est créé dans la base

  Scenario: Extraction avec calcul automatique de priorité
    Given un fichier rollback contenant des incidents avec différents montants
    When le service d'extraction traite le fichier
    Then les incidents avec montant > 10000 ont la priorité "CRITIQUE"
    And les incidents avec montant > 5000 ont la priorité "HAUTE"
    And les incidents avec montant > 1000 ont la priorité "MOYENNE"
    And les autres incidents ont la priorité "FAIBLE"

  Scenario: Traitement automatique du répertoire de surveillance
    Given 3 fichiers rollback sont déposés dans le répertoire de surveillance
    When le job de traitement automatique est déclenché
    Then tous les fichiers sont traités
    And les fichiers sont déplacés vers le répertoire "processed"
    And tous les incidents sont créés et indexés