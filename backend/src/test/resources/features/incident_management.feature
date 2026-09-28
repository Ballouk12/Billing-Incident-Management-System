Feature: Gestion complète des incidents
  En tant qu'utilisateur du système
  Je veux gérer le cycle de vie complet des incidents
  Afin d'assurer un suivi efficace des anomalies

  Background:
    Given un utilisateur "technicien1" avec le rôle "TECHNICIEN"
    And un utilisateur "superviseur1" avec le rôle "SUPERVISEUR"
    And un utilisateur "admin1" avec le rôle "ADMIN"

  Scenario: Création manuelle d'un incident par un superviseur
    Given l'utilisateur "superviseur1" est authentifié
    When il crée un incident avec les données suivantes:
      | incidentType | ERREUR_CALCUL          |
      | description  | Erreur de TVA détectée |
      | priority     | HAUTE                  |
    Then l'incident est créé avec succès
    And le statut initial est "NOUVEAU"
    And un log d'audit "CREATED" est enregistré

  Scenario: Assignation d'un incident à un technicien
    Given l'utilisateur "superviseur1" est authentifié
    And un incident avec l'ID 1 existe avec le statut "NOUVEAU"
    When il assigne l'incident à "technicien1"
    Then le statut de l'incident passe à "EN_COURS"
    And l'incident est assigné à "technicien1"
    And un log d'audit "ASSIGNED" est créé

  Scenario: Ajout d'une solution par un technicien
    Given l'utilisateur "technicien1" est authentifié
    And un incident lui est assigné avec l'ID 1
    When il ajoute une solution avec la description "Correction appliquée sur la facture"
    Then la solution est enregistrée
    And elle est liée à l'incident
    And un historique de la solution est disponible

  Scenario: Résolution d'un incident
    Given l'utilisateur "technicien1" est authentifié
    And un incident avec l'ID 1 est "EN_COURS" et lui est assigné
    And une solution validée existe pour cet incident
    When il marque l'incident comme "RESOLU"
    Then le statut de l'incident passe à "RESOLU"
    And la date de résolution est enregistrée
    And un log d'audit "RESOLVED" est créé

  Scenario: Recherche multi-critères d'incidents
    Given 20 incidents existent dans la base
    And l'utilisateur "superviseur1" est authentifié
    When il recherche des incidents avec les filtres suivants:
      | incidentType | DOUBLON    |
      | status       | EN_COURS   |
      | startDate    | 2025-01-01 |
    Then seuls les incidents correspondants sont retournés
    And les résultats sont paginés

  Scenario: Suppression logique d'un incident par l'admin
    Given l'utilisateur "admin1" est authentifié
    And un incident avec l'ID 1 existe
    When il supprime l'incident
    Then l'incident est marqué comme supprimé (deleted = true)
    And il n'apparaît plus dans les recherches standards
    And un log d'audit "DELETED" est créé

  Scenario: Restriction d'accès pour un technicien
    Given l'utilisateur "technicien1" est authentifié
    And un incident non assigné à lui existe
    When il tente de modifier cet incident
    Then l'accès est refusé
    And une erreur d'autorisation est retournée