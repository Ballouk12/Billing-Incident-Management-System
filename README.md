# Billing Incident Management

## Objectif du projet

Ce projet a été conçu pour centraliser la gestion des incidents de facturation, faciliter leur suivi, leur résolution et leur traçabilité dans une organisation où plusieurs profils collaborent sur les mêmes cas.

L'objectif principal est de fournir une application complète capable de :

- enregistrer et suivre les incidents de facturation,
- attribuer les incidents à des techniciens ou superviseurs,
- documenter les solutions apportées,
- gérer les utilisateurs selon leurs rôles,
- sécuriser les accès via authentification et autorisations,
- traiter les fichiers d'import de données avec traçabilité,
- exploiter des indicateurs de performance et de gestion des incidents.

En pratique, ce projet simule un système de gestion opérationnelle robuste, orienté métier, avec une vraie séparation entre API backend, interface frontend et mécanismes de sécurité, validation et audit.

---

## Principe du projet

Le projet suit une architecture moderne et professionnelle basée sur la logique métier, la séparation des responsabilités et la sécurisation des flux de données.

Il repose sur plusieurs principes fondamentaux :

- séparation claire entre backend et frontend,
- services métier dédiés et testables,
- validation des données avant traitement,
- gestion centralisée des erreurs,
- rôles et permissions explicites,
- audit log pour la traçabilité,
- configuration propre selon l’environnement,
- code lisible, maintenable et évolutif.

Le but est d’aller au-delà d’un simple CRUD : il s’agit d’une application structurée, pensée pour être utilisable en production et pour démontrer des bonnes pratiques de développement Java/Spring Boot et React/Redux.

---

## Stack technique

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- Spring Validation
- JWT pour l’authentification
- MySQL
- Elasticsearch
- Maven

### Frontend
- React
- Vite
- Redux Toolkit
- React Router
- Axios
- Tailwind CSS

---

## Fonctionnalités principales

### 1. Gestion des incidents
- création, consultation, modification et suppression des incidents,
- affectation à un technicien,
- suivi du statut : en attente, en cours, résolu, etc.,
- association de solutions à chaque incident,
- historique de résolution et de traitement..

### 2. Authentification et autorisation
- connexion sécurisée avec JWT,
- gestion des rôles utilisateur : ADMIN, SUPERVISEUR, TECHNICIEN,
- accès aux ressources selon le profil,
- protection des routes côté frontend et contrôle des autorisations côté backend.

### 3. Gestion des fichiers
- import de fichiers de données,
- traitement de fichiers Excel/CSV,
- validation des éléments importés,
- détection des anomalies et exceptions liées au fichier.

### 4. Traçabilité et audit
- journalisation des actions clé (création, modification, affectation, résolution, solution ajoutée),
- suivi de l’utilisateur qui a effectué l’action,
- horodatage des événements,
- visibilité utile pour l’exploitation et le support.

### 5. Recherche et analyse
- recherche d’incidents,
- gestion de filtres,
- indicateurs et statistiques,
- intégration Elasticsearch pour la recherche avancée et la scalabilité.

---

## Architecture du projet

La structure du projet a été pensée pour respecter les bonnes pratiques de découpage d’application.

```text
Billing-Incident/
├── backend/
│   ├── src/main/java/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── listener/
│   │   ├── repository/
│   │   ├── service/
│   │   └── util/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json/
│   └── vite.config.js
├── README.md
└── .gitignore
```

Cette organisation permet :
- de séparer clairement les composants applicatifs,
- de faciliter la maintenance,
- d’isoler la logique métier des couches de présentation,
- de rendre le projet plus lisible et professionnel.

---

## Bonnes pratiques implémentées

### 1. Configuration multi-environnement
Le backend utilise une configuration externe via les variables d’environnement et des profils adaptés selon l’environnement d’exécution.

Exemples concrets du projet :
- application.yml comme fichier de configuration central,
- import de variables depuis un fichier .env,
- paramètres spécifiques pour la base de données, JWT, uploads, Elasticsearch,
- séparation claire entre environnement de développement et production.

Cela permet de sécuriser les secrets, de mieux gérer le déploiement et d’éviter de coder en dur des valeurs sensibles.

### 2. Gestion centralisée des exceptions
Le projet met en place un GlobalExceptionHandler avec des méthodes dédiées pour gérer les différents types d’erreurs.

Exemples gérés :
- ResourceNotFoundException,
- InvalidFileException,
- IllegalArgumentException,
- MethodArgumentNotValidException,
- BadCredentialsException,
- AccessDeniedException,
- erreurs internes générales.

Cette approche permet :
- de standardiser les réponses HTTP,
- d’éviter les erreurs non maîtrisées,
- de fournir des messages clairs à l’utilisateur ou au client API,
- de rendre l’API plus robuste et plus maintenable.

### 3. Sécurité Spring Security + rôles
Le backend applique une configuration de sécurité robuste avec :
- désactivation CSRF pour une API REST stateless,
- gestion CORS,
- authentification JWT,
- autorisations par endpoint,
- rôles d’accès (ADMIN, SUPERVISEUR, TECHNICIEN),
- méthode @PreAuthorize pour sécuriser les actions sensibles.

Cela illustre une bonne pratique de gestion des droits selon le besoin métier, sans exposer les fonctionnalités critiques à tout le monde.

### 4. Contraintes de données et robustesse des entités
Les entités utilisent des contraintes et des annotations de validation pour garantir l’intégrité des données :
- @Column(nullable = false),
- @NotNull,
- contraintes d’unicité sur email et username,
- types stricts et champs obligatoires,
- timestamps automatiques avec @CreationTimestamp et @UpdateTimestamp.

Cette méthode diminue les erreurs de données, renforce la cohérence de la base et améliore la qualité du code.

### 5. Audit log pour la traçabilité
Le projet intègre un système d’audit des actions importantes sur les incidents.

Chaque action est historisée avec :
- l’utilisateur concerné,
- l’incident associé,
- l’action effectuée,
- les détails de changement,
- la date de création,
- l’adresse IP si nécessaire.

C’est une bonne pratique essentielle pour le support, la sécurité, le contrôle de conformité et la compréhension du comportement du système.

### 6. Traitement métier structuré
La logique est répartie dans des services dédiés, avec une claire séparation :
- contrôleurs pour l’API,
- services pour la logique métier,
- repositories pour l’accès aux données,
- DTOs pour la sérialisation et validation,
- entités pour le modèle de persistence.

Cette approche réduit le couplage, facilite les tests et rend l’application plus facile à maintenir.

### 7. Frontend avec Redux centralisé
La partie frontend utilise Redux Toolkit afin de centraliser l’état applicatif.

Principaux avantages :
- gestion centralisée de l’authentification,
- partage des données entre composants,
- réduction des effets de bord dans les composants,
- meilleure cohérence de l’état global,
- code plus propre et plus évolutif.

Le projet met également en place des routes protégées pour restreindre l’accès aux écrans sensibles selon le rôle connecté.

### 8. Protection des routes côté frontend
Le composant PrivateRoute vérifie :
- si l’utilisateur est authentifié,
- si son rôle correspond aux autorisations nécessaires,
- et redirige vers la page appropriée si l’accès est refusé.

Ceci permet de combiner sécurité front et backend pour une expérience utilisateur plus robuste.

---

## Exemples de modules clés

- SecurityConfig : configuration de sécurité, CORS, JWT, routes publiques et privées.
- GlobalExceptionHandler : gestion centralisée des erreurs HTTP et de validation.
- User : entité avec rôles, contraintes et timestamps.
- Incident : entité métier centralement utilisée pour le traitement des incidents.
- AuditLog : historique des actions utilisateurs.
- authSlice : gestion de l’état d’authentification côté frontend.
- incidentSlice : gestion des données incidents dans Redux.
- PrivateRoute : protection des routes selon le rôle.

---

## Déploiement et exécution

### Backend
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Variables d’environnement
Les paramètres sensibles sont stockés via des variables d’environnement, notamment :
- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- ELASTICSEARCH_URL
- ELASTICSEARCH_USERNAME
- ELASTICSEARCH_PASSWORD
- UPLOAD_DIR
- ROLLBACK_DIR

---

## Résultat attendu

Ce projet montre une capacité réelle à concevoir un système d’application backend/frontend complet avec :
- sécurité,
- gestion des droits,
- validation forte,
- architecture professionnelle,
- traçabilité,
- qualité de code,
- bonnes pratiques de développement Java/Spring et React.

C’est un projet de qualité idéale pour illustrer un profil technique solide, orienté solution, robuste et prêt pour des contextes professionnels réels.

---

## Conclusion

Ce projet reflète une approche de développement orientée produit et qualité logicielle : il ne se contente pas de mettre en place des fonctionnalités, il s’assure également que le système est sûr, traçable, évolutif et bien structuré.

Il me permet de mettre en valeur mon profil en tant que développeur capable de prendre en charge des applications métier complètes, avec une attention particulière à la sécurité, à la fiabilité et à la maintenabilité.
