Feature: Suivi et historique des solutions
  En tant que technicien ou superviseur
  Je veux suivre l'historique des solutions appliquées
  Afin de capitaliser sur les résolutions passées

  Background:
    Given un utilisateur "technicien1" avec le rôle "TECHNICIEN"
    And un incident avec l'ID 1 existe

  Scenario: Ajout d'une solution avec commentaires
    Given l'utilisateur "technicien1" est authentifié
    And l'incident 1 lui est assigné
    When il ajoute une solution avec:
      | description  | Recalcul de la TVA effectué       |
      | solutionType | CORRECTION                        |
      | comments     | Appliqué sur toutes les lignes    |
    Then la solution est enregistrée
    And elle est associée à l'incident 1
    And l'auteur de la solution est "technicien1"

  Scenario: Consultation de l'historique des solutions d'un incident
    Given l'incident 1 a 3 solutions enregistrées
    And l'utilisateur "technicien1" est authentifié
    When il consulte l'historique des solutions de l'incident
    Then toutes les solutions sont retournées
    And elles sont triées par date de création décroissante
    And chaque solution affiche l'auteur et la date

  Scenario: Validation d'une solution par un superviseur
    Given une solution existe pour l'incident 1
    And l'utilisateur "superviseur1" est authentifié
    When il valide la solution
    Then le champ "validated" passe à "true"
    And un log d'audit est créé

  Scenario: Recherche de solutions similaires pour incidents futurs
    Given plusieurs incidents de type "DOUBLON" ont des solutions validées
    And un nouvel incident de type "DOUBLON" est créé
    When l'utilisateur consulte l'incident
    Then le système suggère les solutions similaires
    And l'historique des résolutions passées est accessible

  Scenario: Ajout de plusieurs solutions pour un même incident
    Given l'incident 1 est "EN_COURS"
    And l'utilisateur "technicien1" est authentifié
    When il ajoute une première solution
    And puis une deuxième solution complémentaire
    Then les deux solutions sont enregistrées
    And elles sont distinctes dans l'historique
    And l'ordre chronologique est préservé