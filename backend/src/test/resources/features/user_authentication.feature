Feature: Authentification et gestion des utilisateurs
  En tant qu'utilisateur du système
  Je veux m'authentifier de manière sécurisée
  Afin d'accéder aux fonctionnalités selon mon rôle

  Scenario: Connexion réussie avec identifiants valides
    Given un utilisateur "john.doe" existe avec le mot de passe "SecurePass123"
    When il se connecte avec ses identifiants
    Then un token JWT est généré
    And le token contient le username et le rôle
    And la durée de validité est de 24 heures
    And une réponse de succès est retournée

  Scenario: Échec de connexion avec mot de passe incorrect
    Given un utilisateur "john.doe" existe
    When il tente de se connecter avec un mot de passe incorrect
    Then la connexion est refusée
    And aucun token n'est généré
    And un message d'erreur "Identifiants invalides" est retourné

  Scenario: Accès à une ressource protégée avec token valide
    Given un utilisateur authentifié avec un token JWT valide
    When il accède à l'endpoint "/api/incidents"
    Then la requête est acceptée
    And les données sont retournées

  Scenario: Refus d'accès sans token
    Given un utilisateur non authentifié
    When il tente d'accéder à "/api/incidents"
    Then la requête est rejetée avec le code 401
    And un message d'erreur d'authentification est retourné

  Scenario: Création d'un utilisateur par l'admin
    Given un administrateur est authentifié
    When il crée un nouvel utilisateur avec les données:
      | username  | new.tech         |
      | email     | tech@company.com |
      | role      | TECHNICIEN       |
      | firstName | John             |
      | lastName  | Smith            |
    Then l'utilisateur est créé avec succès
    And le mot de passe est chiffré avec BCrypt
    And l'utilisateur est actif par défaut
    And un email de bienvenue est envoyé (mock)

  Scenario: Désactivation d'un utilisateur par l'admin
    Given un utilisateur "john.doe" actif existe
    And un administrateur est authentifié
    When il désactive l'utilisateur
    Then l'utilisateur ne peut plus se connecter
    And son statut actif passe à "false"

  Scenario: Réinitialisation de mot de passe par l'admin
    Given un utilisateur "john.doe" existe
    And un administrateur est authentifié
    When il réinitialise le mot de passe de l'utilisateur
    Then un nouveau mot de passe temporaire est généré
    And le mot de passe est retourné à l'admin
    And l'utilisateur peut se connecter avec le nouveau mot de passe