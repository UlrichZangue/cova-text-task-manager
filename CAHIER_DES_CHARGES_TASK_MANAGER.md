# Cahier des charges — Task Manager

**Version : 1.0**  
**Type : Test technique de recrutement**  
**Architecture : Monorepo**  
**Plateformes : Web + Mobile**  
**Backend : Java / Spring Boot**  
**Frontend : React / Vite / TypeScript**  
**Mobile : Flutter / Dart — Bonus**  
**Base de données : MySQL**  
**Déploiement : Docker + GCP — Bonus**

---

## 1. Présentation du projet

Le projet consiste à développer une application moderne de gestion personnelle des tâches permettant à un utilisateur de créer un compte, se connecter et gérer ses tâches depuis une interface web.

L'application devra également être conçue de manière à pouvoir être consommée par une application mobile Flutter utilisant la même API backend.

Le périmètre de base comprend l'authentification, le CRUD des tâches, la recherche, le filtrage, la synchronisation web/mobile, une API REST Spring Boot, MySQL, JWT et un frontend React/Vite/TypeScript.

Le déploiement Docker/GCP, Flutter et CI/CD constituent des bonus.

---

## 2. Objectifs

### 2.1 Objectif principal

Développer une application **Task Manager fonctionnelle, sécurisée, responsive, documentée et facilement déployable**, démontrant la maîtrise :

- du développement backend Java/Spring Boot ;
- du développement frontend React/TypeScript ;
- de la conception d'une API REST ;
- de la persistance MySQL/JPA ;
- de l'authentification JWT ;
- des bonnes pratiques d'architecture ;
- des tests ;
- de Docker ;
- du CI/CD ;
- du développement mobile Flutter en bonus.

### 2.2 Objectifs secondaires

Le projet devra également démontrer :

- une architecture propre ;
- une séparation claire des responsabilités ;
- une bonne gestion des erreurs ;
- une validation des données ;
- une interface agréable ;
- une bonne expérience utilisateur ;
- une documentation exploitable ;
- une approche orientée production.

---

## 3. Classification des fonctionnalités

Les fonctionnalités sont réparties en quatre catégories :

- 🔴 **Obligatoire** : explicitement demandée dans le test.
- 🟢 **Recommandé** : ajout destiné à améliorer la qualité du projet.
- 🔵 **Bonus** : fonctionnalité bonus prévue dans le test.
- ⚪ **Optionnel** : à développer uniquement si le temps le permet.

---

# 4. Gestion des utilisateurs

## 4.1 Inscription — 🔴 Obligatoire

L'utilisateur doit pouvoir créer un compte.

### Informations

- Nom
- Email
- Mot de passe
- Confirmation du mot de passe

### Contraintes

- email obligatoire ;
- email valide ;
- email unique ;
- mot de passe obligatoire ;
- confirmation identique au mot de passe ;
- mot de passe stocké sous forme hashée.

### Endpoint

```http
POST /api/auth/register
```

---

# 5. Authentification

## 5.1 Connexion — 🔴 Obligatoire

L'utilisateur doit pouvoir se connecter avec :

- Email
- Mot de passe

### Endpoint

```http
POST /api/auth/login
```

Le serveur retourne un JWT.

## 5.2 Gestion du JWT — 🔴 Obligatoire

Pour la première version, le frontend conserve le token dans un cookie http only.

Les requêtes protégées utilisent :

```http
Authorization: Bearer <JWT>
```

Toutes les routes privées doivent vérifier le token.

## 5.3 Déconnexion — 🟢 Recommandé

Prévoir une déconnexion côté frontend :

- suppression du JWT ;
- nettoyage de l'état utilisateur ;
- redirection vers `/login`.

---

# 6. Gestion des tâches

## 6.1 Création — 🔴 Obligatoire

L'utilisateur connecté peut créer une tâche.

### Champs minimum

- `title`
- `description`
- `status`

### Endpoint

```http
POST /api/tasks
```

## 6.2 Modification — 🔴 Obligatoire

```http
PUT /api/tasks/{id}
```

Le backend doit vérifier que la tâche appartient à l'utilisateur connecté.

## 6.3 Suppression — 🔴 Obligatoire

```http
DELETE /api/tasks/{id}
```

Le backend doit empêcher la suppression d'une tâche appartenant à un autre utilisateur.

## 6.4 Consultation — 🔴 Obligatoire

```http
GET /api/tasks
```

Le serveur doit retourner uniquement les tâches de l'utilisateur connecté.

---

# 7. Statuts des tâches

## 🔴 Obligatoire

Définir une énumération :

```text
TODO
IN_PROGRESS
DONE
```

Libellés UI :

```text
À faire
En cours
Terminée
```

---

# 8. Priorité des tâches — 🟢 Recommandé

Ajouter :

```text
LOW
MEDIUM
HIGH
```

Exemple :

```text
Préparer le rapport

Priorité : HIGH
Statut : En cours
```

---

# 9. Date d'échéance — 🟢 Recommandé

Ajouter :

```text
dueDate
```

L'application pourra distinguer :

- À venir
- Échéance proche
- En retard
- Terminée

---

# 10. Dates système

Chaque tâche doit contenir :

```text
createdAt
updatedAt
```

Ces champs font partie des exigences du test.

---

# 11. Recherche — 🔴 Obligatoire

L'utilisateur peut rechercher une tâche par son contenu.

Exemple :

```http
GET /api/tasks?search=rapport
```

La recherche peut porter au minimum sur le titre.

---

# 12. Filtrage — 🔴 Obligatoire

Filtrage par statut :

```text
Toutes
À faire
En cours
Terminées
```

## 12.1 Filtrage par priorité — 🟢 Recommandé

```text
Toutes
Haute
Moyenne
Basse
```

## 12.2 Combinaison recherche + filtres — 🟢 Recommandé

Exemple :

```http
GET /api/tasks?search=rapport&status=TODO&priority=HIGH
```

---

# 13. Pagination — 🟢 Recommandé

Prévoir :

```http
GET /api/tasks?page=0&size=10
```

Exemple de réponse :

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

La pagination doit être gérée avec Spring Data JPA.

---

# 14. Dashboard — 🟢 Recommandé

Après connexion, l'utilisateur arrive sur un dashboard.

### Statistiques

- Nombre total de tâches
- Nombre de tâches à faire
- Nombre de tâches en cours
- Nombre de tâches terminées

Exemple :

```text
┌────────────┬────────────┬────────────┬────────────┐
│   TOTAL    │   À FAIRE  │  EN COURS  │ TERMINÉES  │
│     24     │     8      │     6      │     10     │
└────────────┴────────────┴────────────┴────────────┘
```

---

# 15. UX/UI

## 15.1 Interface moderne — 🟢 Recommandé

Technologies :

- React
- TypeScript
- Vite
- Tailwind CSS et/ou Shadcn UI

## 15.2 Responsive design — 🟢 Recommandé

L'application web doit être utilisable sur :

- Desktop
- Tablette
- Smartphone

## 15.3 Dark mode — 🟢 Recommandé

Prévoir un choix :

```text
☀️ Light
🌙 Dark
```

---

# 16. États de l'interface — 🟢 Recommandé

Chaque écran important doit gérer :

### Loading

```text
Chargement...
```

### Empty state

```text
Vous n'avez aucune tâche.

[Créer ma première tâche]
```

### Error state

```text
Impossible de charger les tâches.

[Réessayer]
```

---

# 17. Notifications utilisateur — 🟢 Recommandé

Utiliser des toasts pour :

```text
✓ Tâche créée
✓ Tâche modifiée
✓ Tâche supprimée
✓ Connexion réussie
✕ Identifiants incorrects
✕ Erreur serveur
```

---

# 18. Architecture Backend

Architecture en couches :

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
MySQL
```

Organisation recommandée :

```text
auth/
user/
task/
security/
config/
exception/
```

---

# 19. Modèle de données

## 19.1 User

```text
User
├── id
├── name
├── email
├── password
└── createdAt
```

## 19.2 Task

```text
Task
├── id
├── title
├── description
├── status
├── priority
├── dueDate
├── createdAt
├── updatedAt
└── user
```

### Relation

```text
User 1 ─────────── N Task
```

Chaque tâche appartient à un utilisateur.

---

# 20. DTOs — 🟢 Recommandé

Ne pas exposer directement les entités JPA dans les API.

Prévoir :

```text
RegisterRequest
LoginRequest
AuthResponse

TaskRequest
TaskResponse
```

Architecture :

```text
HTTP
 ↓
DTO
 ↓
Service
 ↓
Entity
 ↓
Repository
```

---

# 21. Validation — 🟢 Recommandé

Utiliser Bean Validation :

```text
@NotBlank
@Email
@Size
@NotNull
```

Le backend doit rejeter les données invalides avec des messages explicites.

---

# 22. Gestion globale des exceptions — 🟢 Recommandé

Créer :

```text
GlobalExceptionHandler
```

Format d'erreur uniforme :

```json
{
  "timestamp": "2026-09-16T20:30:00",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Task not found",
  "path": "/api/tasks/42"
}
```

---

# 23. Sécurité

## 🔴 Obligatoire

- Spring Security
- JWT

## 🟢 Recommandé

- BCrypt pour les mots de passe ;
- validation stricte des accès ;
- CORS configuré ;
- endpoints publics limités ;
- endpoints privés protégés.

---

# 24. API REST

## Authentification

```text
POST /api/auth/register
POST /api/auth/login
```

## Tâches

```text
GET    /api/tasks
GET    /api/tasks/{id}
POST   /api/tasks
PUT    /api/tasks/{id}
DELETE /api/tasks/{id}
```

---

# 25. Swagger / OpenAPI — 🟢 Recommandé

Documenter toutes les routes :

- paramètres ;
- body ;
- réponses ;
- erreurs ;
- authentification JWT.

Prévoir une interface Swagger accessible depuis le backend.

---

# 26. Tests backend — 🟢 Recommandé

## Unit tests

```text
AuthServiceTest
TaskServiceTest
```

## Integration/API tests

```text
AuthControllerTest
TaskControllerTest
```

### Scénarios minimum

- inscription réussie ;
- email déjà utilisé ;
- login réussi ;
- mauvais mot de passe ;
- création de tâche ;
- modification de tâche ;
- suppression de tâche ;
- récupération des tâches ;
- accès à la tâche d'un autre utilisateur refusé ;
- validation des données.

L'objectif est d'avoir environ 10 à 20 tests pertinents plutôt qu'un grand nombre de tests artificiels.

---

# 27. Architecture Frontend

Structure recommandée :

```text
frontend/
└── src/
    ├── components/
    ├── pages/
    ├── services/
    ├── hooks/
    ├── types/
    ├── routes/
    └── utils/
```

---

# 28. Pages Web

## 🔴 Obligatoire

```text
/login
/register
/tasks
```

## 🟢 Recommandé

```text
/dashboard
/profile
```

---

# 29. Composants Frontend

Prévoir notamment :

```text
TaskCard
TaskForm
TaskList
TaskFilter
SearchBar
Navbar
Sidebar
StatsCard
ConfirmDialog
LoadingSpinner
EmptyState
ErrorState
```

---

# 30. Gestion d'état

Pour le MVP, utiliser une solution simple :

- React Context ;
- hooks React ;
- état local lorsque suffisant.

Ne pas introduire Redux uniquement pour augmenter artificiellement la complexité.

---

# 31. Service API

Créer une couche dédiée :

```text
api.ts
authService.ts
taskService.ts
```

Architecture :

```text
React Component
       ↓
taskService
       ↓
API Client
       ↓
Spring Boot
```

---

# 32. Application Mobile Flutter — 🔵 Bonus

L'application mobile doit utiliser la même API backend.

### Fonctionnalités

- Login
- Affichage des tâches
- Création
- Modification
- Suppression
- Gestion du statut
- JWT

### Technologies

- Flutter
- Dart
- Dio ou http
- ListView
- TextField
- ElevatedButton

---

# 33. Synchronisation Web/Mobile — 🔵 Bonus

Il ne doit pas y avoir deux bases de données.

```text
             ┌─────────────┐
             │    MySQL    │
             └──────▲──────┘
                    │
             Spring Boot API
                ▲       ▲
                │       │
             React    Flutter
```

Les deux applications consomment la même API et les mêmes données.

---

# 34. Soft Delete — ⚪ Optionnel

Ajouter :

```text
deletedAt
```

Au lieu de supprimer physiquement une tâche, celle-ci peut être marquée comme supprimée.

Cette fonctionnalité reste secondaire et ne doit être développée qu'après le périmètre prioritaire.

---

# 35. Historique des modifications — ⚪ Optionnel

Si toutes les fonctionnalités prioritaires sont terminées :

```text
TaskHistory
├── id
├── taskId
├── action
├── userId
└── createdAt
```

Actions possibles :

```text
CREATED
UPDATED
DELETED
STATUS_CHANGED
```

Ne pas développer un système d'audit complexe pour ce test.

---

# 36. Docker — 🔵 Bonus

Prévoir :

```text
backend/Dockerfile
frontend/Dockerfile
docker-compose.yml
```

Architecture locale :

```text
Docker Compose
│
├── frontend
├── backend
└── mysql
```

---

# 37. Variables d'environnement

Aucune information sensible ne doit être hardcodée.

Prévoir notamment :

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
JWT_SECRET
API_URL
```

Ajouter :

```text
.env.example
```

Le fichier `.env` réel ne doit jamais être commité dans Git.

---

# 38. CI/CD — 🔵 Bonus

Créer :

```text
.github/workflows/ci-cd.yml
```

Pipeline :

```text
Push / Pull Request
        ↓
Installation des dépendances
        ↓
Tests
        ↓
Build
        ↓
Docker Build
        ↓
Publication des images
        ↓
Déploiement GCP
```

Le pipeline doit au minimum construire et tester le backend et le frontend.

---

# 39. Déploiement GCP — 🔵 Bonus

Le test prévoit notamment :

- Google Cloud Run ;
- ou Google Compute Engine.

Architecture cible :

```text
Internet
   ↓
Frontend
   ↓
Spring Boot API
   ↓
MySQL
```

Le choix précis de l'hébergement MySQL devra être documenté dans le README.

---

# 40. Documentation

## 🔴 Obligatoire

Créer :

```text
README.md
```

Le README doit contenir :

1. Présentation
2. Fonctionnalités
3. Architecture
4. Stack technique
5. Prérequis
6. Installation
7. Configuration
8. Lancement backend
9. Lancement frontend
10. Lancement Docker
11. Documentation API
12. Swagger
13. Tests
14. CI/CD
15. Déploiement
16. Captures d'écran
17. Choix techniques
18. Limites connues

---

# 41. Git

Structure recommandée :

```text
main
develop
feature/*
```

Convention de commits :

```text
feat: add task creation
fix: handle invalid jwt
test: add task service tests
docs: update installation guide
refactor: improve task service
```

---

# 42. Structure finale du repository

```text
task-manager/
│
├── backend/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   ├── package.json
│   └── README.md
│
├── mobile/                    # BONUS
│   ├── lib/
│   └── pubspec.yaml
│
├── .github/
│   └── workflows/
│       └── ci-cd.yml
│
├── docker-compose.yml
├── .env.example
├── .gitignore
├── README.md
└── LICENSE
```

---

# 43. Fonctionnalités exclues du périmètre

Pour conserver un projet maîtrisable :

- Chat ;
- WebSocket ;
- notifications push ;
- multi-tenant ;
- gestion complexe des équipes ;
- microservices ;
- Kubernetes ;
- Elasticsearch ;
- IA ;
- système de permissions complexe ;
- calendrier complet.

Ces fonctionnalités peuvent être envisagées dans un futur projet mais ne sont pas nécessaires pour le test.

---

# 44. Plan de réalisation

## Phase 1 — 🔴 MVP obligatoire

```text
✓ Architecture
✓ Spring Boot
✓ MySQL
✓ User
✓ Task
✓ JWT
✓ Register
✓ Login
✓ CRUD Task
✓ React
✓ Login/Register
✓ Liste
✓ Création
✓ Modification
✓ Suppression
✓ Recherche
✓ Filtre statut
```

**Objectif : application fonctionnelle de bout en bout.**

---

## Phase 2 — 🟢 Qualité et différenciation

```text
✓ DTO
✓ Validation
✓ Exception Handler
✓ BCrypt
✓ Pagination
✓ Priorité
✓ Due date
✓ Dashboard
✓ Loading states
✓ Empty states
✓ Error states
✓ Toasts
✓ Responsive
✓ Dark mode
```

**Objectif : transformer le MVP en application professionnelle.**

---

## Phase 3 — 🟢 Qualité technique

```text
✓ Swagger
✓ Unit tests
✓ Integration tests
✓ Documentation API
✓ .env.example
✓ README complet
✓ Git propre
```

**Objectif : démontrer la qualité d'ingénierie.**

---

## Phase 4 — 🔵 Bonus du test

```text
✓ Docker
✓ Docker Compose
✓ GitHub Actions
✓ Flutter
✓ Synchronisation mobile
✓ GCP
```

---

## Phase 5 — ⚪ Optionnel

Seulement si toutes les phases précédentes sont terminées :

```text
○ Soft delete
○ Historique des modifications
○ Profil utilisateur
○ Export CSV
```

---

# 45. Critères d'acceptation

## Fonctionnel

- [ ] Un utilisateur peut s'inscrire.
- [ ] Un utilisateur peut se connecter.
- [ ] Un utilisateur peut se déconnecter.
- [ ] Un utilisateur peut créer une tâche.
- [ ] Un utilisateur peut modifier une tâche.
- [ ] Un utilisateur peut supprimer une tâche.
- [ ] Un utilisateur peut consulter ses tâches.
- [ ] Les tâches des autres utilisateurs sont inaccessibles.
- [ ] La recherche fonctionne.
- [ ] Le filtrage par statut fonctionne.
- [ ] Le filtrage par priorité fonctionne.
- [ ] Les dates d'échéance sont affichées correctement.

## Backend

- [ ] API REST fonctionnelle.
- [ ] Spring Security.
- [ ] JWT.
- [ ] BCrypt.
- [ ] Spring Data JPA.
- [ ] MySQL.
- [ ] DTOs.
- [ ] Validation.
- [ ] Gestion globale des exceptions.
- [ ] Swagger/OpenAPI.
- [ ] Tests automatisés.

## Frontend

- [ ] React.
- [ ] Vite.
- [ ] TypeScript/TSX.
- [ ] Tailwind/Shadcn UI.
- [ ] Interface responsive.
- [ ] Dashboard.
- [ ] Dark mode.
- [ ] Loading states.
- [ ] Empty states.
- [ ] Error states.
- [ ] Notifications/toasts.

## DevOps

- [ ] Dockerfile backend.
- [ ] Dockerfile frontend.
- [ ] Docker Compose.
- [ ] GitHub Actions.
- [ ] Tests dans le pipeline.
- [ ] Build Docker.
- [ ] Déploiement GCP.

## Mobile

- [ ] Application Flutter.
- [ ] Connexion JWT.
- [ ] Liste des tâches.
- [ ] Création de tâches.
- [ ] Modification.
- [ ] Suppression.
- [ ] Synchronisation avec l'API.

## Documentation

- [ ] README complet.
- [ ] Architecture documentée.
- [ ] Instructions d'installation.
- [ ] Variables d'environnement documentées.
- [ ] API documentée.
- [ ] Swagger disponible.
- [ ] Captures d'écran.
- [ ] Instructions Docker.
- [ ] Instructions de déploiement.

---

# 46. Architecture finale

```text
                         TASK MANAGER
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
       WEB APP             MOBILE             API
       React              Flutter          Spring Boot
       Vite               Dart             REST
       TypeScript         Dio              JWT
       Tailwind
          │                   │
          └────────┬──────────┘
                   │
                   ▼
            SPRING BOOT API
                   │
       ┌───────────┼───────────┐
       │           │           │
       ▼           ▼           ▼
     Auth         Tasks      Security
       │           │           │
       └───────────┼───────────┘
                   │
                   ▼
                  JPA
                   │
                   ▼
                 MySQL

       QUALITÉ
       ├── DTO
       ├── Validation
       ├── Exception Handler
       ├── Swagger
       ├── Tests
       └── Pagination

       UX
       ├── Dashboard
       ├── Recherche
       ├── Filtres
       ├── Priorités
       ├── Échéances
       ├── Responsive
       └── Dark Mode

       DEVOPS
       ├── Docker
       ├── Docker Compose
       ├── GitHub Actions
       └── GCP
```

---

# 47. Principe directeur

Le projet doit rester **simple dans son cœur et professionnel dans son exécution**.

Le cœur fonctionnel reste :

```text
User
  +
Task
  +
Authentication
  +
CRUD
```

La différenciation vient principalement de :

```text
Architecture
Validation
Sécurité
UX
Tests
Documentation
Swagger
Docker
CI/CD
```

Les fonctionnalités secondaires ne doivent jamais compromettre la stabilité du MVP.

---

# 48. Résultat attendu

À la fin du projet, le repository doit présenter une application capable de :

1. créer et authentifier un utilisateur ;
2. sécuriser ses données avec JWT ;
3. gérer ses tâches ;
4. rechercher et filtrer ses tâches ;
5. gérer priorité et échéance ;
6. présenter un dashboard ;
7. fonctionner sur desktop et mobile web ;
8. être consommée par une application Flutter ;
9. être testée automatiquement ;
10. être documentée avec Swagger et README ;
11. être exécutée avec Docker ;
12. être intégrée dans un pipeline CI/CD ;
13. être déployée sur GCP en bonus.

**Priorité absolue : stabilité et qualité du MVP avant l'ajout des bonus et fonctionnalités optionnelles.**
