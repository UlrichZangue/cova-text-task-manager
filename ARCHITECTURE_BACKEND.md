# Architecture du backend Task Manager

## 1. Objectif du document

Ce document decrit l'architecture technique du backend Task Manager, les
responsabilites de chaque couche et les principaux flux d'execution. Il sert de
reference pour maintenir ou faire evoluer l'API sans casser ses frontieres.

Le backend est une API REST stateless construite avec Java 21, Spring Boot 4,
Spring Security, Spring Data JPA, MySQL, Flyway, JWT et Docker Compose.

## 2. Vue d'ensemble

L'application utilise une architecture en couches organisee par domaine. Les
trois domaines fonctionnels sont `auth`, `user` et `task`. Les concerns
transversaux sont regroupes dans `config`, `security` et `exception`.

```text
Client HTTP
    |
    v
Spring Security / filtre JWT
    |
    v
Controller REST
    |
    v
Service metier
    |
    +----> Mapper DTO <-> entite
    |
    v
Repository Spring Data JPA
    |
    v
MySQL
```

Principes structurants :

- les controleurs gerent HTTP et deleguent le metier ;
- les services portent les regles metier et les transactions ;
- les repositories isolent l'acces aux donnees ;
- les entites JPA ne sont jamais exposees directement par l'API ;
- les DTO definissent les contrats d'entree et de sortie ;
- l'identite de l'utilisateur vient toujours du JWT ;
- toutes les routes metier sont protegees par defaut ;
- Flyway est l'unique proprietaire du schema de base de donnees.

## 3. Organisation des sources

```text
backend/src/main/java/com/taskmanager/backend
|-- TaskManagerApplication.java
|-- auth/
|   |-- controller/       endpoints d'inscription et de connexion
|   |-- dto/              contrats HTTP d'authentification
|   `-- service/          inscription, verification et emission du JWT
|-- user/
|   |-- controller/       endpoint du profil connecte
|   |-- dto/              projection publique du profil
|   |-- entity/           entite User
|   |-- repository/       acces aux utilisateurs
|   `-- service/          lecture du profil authentifie
|-- task/
|   |-- controller/       CRUD, recherche et statistiques
|   |-- dto/              requetes, reponses, pages et statistiques
|   |-- entity/           Task et enumerations
|   |-- mapper/           conversion DTO/entite
|   |-- repository/       requetes JPA et projections
|   `-- service/          regles metier des taches
|-- security/             JWT, filtre, UserDetails et erreurs de securite
|-- exception/            exceptions et contrat d'erreur commun
`-- config/               Spring Security, CORS et OpenAPI
```

Les migrations SQL sont dans :

```text
backend/src/main/resources/db/migration
|-- V1__initial_schema.sql
`-- V2__stabilize_schema.sql
```

## 4. Responsabilites des couches

### 4.1 Controllers

Les controllers constituent la frontiere HTTP. Ils :

- declarent les routes et codes de statut ;
- valident les corps avec Bean Validation et `@Valid` ;
- lisent l'identite depuis l'objet `Authentication` ;
- appellent un service ;
- retournent uniquement des DTO ;
- portent les annotations OpenAPI.

Ils ne contiennent ni requete SQL, ni regle de propriete, ni construction de JWT.

Controllers actuels :

| Controller | Prefixe | Responsabilite |
|---|---|---|
| `AuthController` | `/api/auth` | inscription et connexion |
| `UserController` | `/api/users` | profil de l'utilisateur connecte |
| `TaskController` | `/api/tasks` | CRUD, recherche, pagination et statistiques |

### 4.2 Services

Les services portent les cas d'utilisation et les transactions.

- `AuthService` normalise les e-mails, controle les doublons, chiffre les mots
  de passe, authentifie les identifiants et demande l'emission du JWT.
- `UserService` retrouve l'utilisateur correspondant au principal JWT et produit
  son profil public.
- `TaskService` impose la propriete des taches, applique la suppression logique,
  valide pagination et tri, puis orchestre repository et mapper.

Les lectures sont executees dans des transactions `readOnly`. Les creations,
modifications et suppressions ouvrent une transaction en ecriture.

### 4.3 Repositories

Les repositories etendent les interfaces Spring Data JPA.

- `UserRepository` fournit la recherche et le controle d'unicite par e-mail.
- `TaskRepository` fournit les acces par proprietaire, les statistiques et les
  operations necessaires au CRUD.
- `TaskSpecifications` construit dynamiquement les predicats de recherche par
  texte, statut, priorite, proprietaire et absence de suppression logique.

La pagination et le tri sont delegues a Spring Data via `PageRequest` et `Sort`.

### 4.4 Entites

`User` represente un compte. Ses donnees principales sont l'identifiant UUID, le
nom, l'e-mail unique, le hash BCrypt et la date de creation.

`Task` represente une tache appartenant a un utilisateur. Elle contient le titre,
la description, le statut, la priorite, l'echeance, les dates techniques et une
date de suppression logique.

Relation principale :

```text
User 1 -------- N Task
```

Une tache possede une cle etrangere `user_id` obligatoire. La suppression HTTP
d'une tache renseigne `deleted_at` au lieu de supprimer physiquement la ligne.

### 4.5 DTO et mapping

Les DTO protegent le modele de persistance et stabilisent le contrat HTTP.

- les DTO de requete portent les contraintes de validation ;
- les DTO de reponse excluent les associations JPA et donnees sensibles ;
- `TaskMapper` centralise la conversion entre `TaskRequest`, `Task` et
  `TaskResponse` ;
- `PageResponse<T>` fournit un format de pagination independant de Spring ;
- `UserProfileResponse` n'expose ni mot de passe ni collection de taches.

## 5. Architecture de securite

L'API est stateless. Aucune session HTTP et aucun cookie d'authentification ne
sont utilises.

### Routes publiques

- `POST /api/auth/register`
- `POST /api/auth/login`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `GET /actuator/health`

Toutes les autres routes necessitent un JWT valide.

### Flux d'authentification

```text
1. Client -> POST /api/auth/login avec email et mot de passe
2. AuthService -> AuthenticationManager
3. CustomUserDetailsService -> UserRepository
4. PasswordEncoder -> verification BCrypt
5. JwtService -> generation d'un token signe
6. API -> token Bearer et duree d'expiration
```

### Flux d'une requete protegee

```text
1. Le client envoie Authorization: Bearer <token>
2. JwtAuthenticationFilter extrait et valide le token
3. CustomUserDetailsService recharge l'utilisateur
4. Le filtre place l'Authentication dans le SecurityContext
5. Le controller recupere authentication.getName()
6. Le service utilise cet email pour limiter l'acces aux donnees du proprietaire
```

`JwtService` valide le secret au demarrage. Le secret doit contenir au moins 32
octets et sa valeur provient exclusivement de `JWT_SECRET`.

La politique CORS est configuree par `CORS_ALLOWED_ORIGINS`. Elle autorise les
methodes necessaires a l'API et les en-tetes `Authorization` et `Content-Type`.

## 6. Isolation des donnees

Le client ne transmet jamais de `userId` pour lire ou modifier une tache. Le
service retrouve l'utilisateur a partir de l'e-mail authentifie, puis execute des
requetes qui combinent identifiant de tache, proprietaire et `deleted_at IS NULL`.

Cette strategie evite les acces horizontaux entre utilisateurs. Une tache
inexistante, supprimee ou appartenant a un autre utilisateur produit la meme
reponse `404`, sans confirmer l'existence d'une ressource etrangere.

## 7. Gestion des erreurs

`GlobalExceptionHandler` transforme les exceptions applicatives et Spring MVC
en un contrat JSON commun. `SecurityErrorHandler` produit le meme contrat pour
les erreurs generees avant l'appel d'un controller.

Format general :

```json
{
  "timestamp": "2026-09-17T18:00:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid request",
  "path": "/api/tasks",
  "fieldErrors": {
    "title": "Title is required"
  }
}
```

Correspondances principales :

| Situation | Code HTTP |
|---|---:|
| validation ou parametre incorrect | `400` |
| JWT absent, invalide ou expire | `401` |
| acces refuse | `403` |
| ressource introuvable | `404` |
| methode HTTP incorrecte | `405` |
| e-mail deja utilise | `409` |
| erreur inattendue | `500` |

Les erreurs `500` ne renvoient jamais la stack trace au client.

## 8. Persistance et migrations

MySQL 8.4 stocke les utilisateurs et les taches. Hibernate utilise
`ddl-auto=validate` : il verifie la coherence entre les entites et le schema sans
modifier ce dernier.

Flyway applique les migrations versionnees au demarrage :

- `V1` cree les tables `users` et `tasks` avec leur relation ;
- `V2` stabilise les tailles de colonnes et ajoute les index d'unicite, de
  proprietaire, de statut, de priorite et de suppression logique.

Les identifiants sont des UUID stockes sous forme `BINARY(16)`.

## 9. Recherche, pagination et statistiques

La liste des taches combine :

- recherche partielle dans le titre ;
- filtre par statut ;
- filtre par priorite ;
- pagination bornee a 100 elements ;
- tri limite a une liste blanche de proprietes ;
- filtre permanent par proprietaire et par suppression logique.

Les statistiques sont calculees en base et retournees sous forme de projection
`TaskStatsResponse`, sans charger toutes les taches en memoire.

## 10. Documentation de l'API

Springdoc genere le contrat OpenAPI depuis les controllers et DTO.

- Swagger UI : `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON : `http://localhost:8080/v3/api-docs`

Le schema `bearerAuth` indique que les routes protegees attendent un JWT dans
l'en-tete `Authorization`.

## 11. Architecture des tests

La suite comprend des tests unitaires et des tests d'integration MockMvc.

- tests de validation des DTO ;
- tests unitaires des services, du mapper et du JWT ;
- tests du filtre et des erreurs Spring Security ;
- tests HTTP d'inscription, connexion, profil et CRUD ;
- tests de propriete des ressources et de suppression logique ;
- tests de recherche, filtres, pagination, tri et statistiques ;
- test du contrat OpenAPI.

Les tests d'integration chargent le vrai contexte Spring et utilisent MySQL dans
Docker. Les transactions de test sont annulees afin d'isoler les scenarios.

Commande de reference :

```bash
docker compose --profile test run --rm test
```

## 12. Architecture Docker

Docker Compose orchestre trois services :

| Service | Role |
|---|---|
| `backend` | construit et execute l'API sur le port `8080` |
| `mysql` | stocke les donnees dans le volume `mysql_data` |
| `test` | execute Maven et la suite de tests avec le profil Compose `test` |

Le backend attend que MySQL soit sain avant de demarrer. Son image utilise un
build multi-stage : Maven produit le JAR, puis une image JRE minimale l'execute
avec l'utilisateur non-root `app`.

Le healthcheck du backend appelle `/actuator/health`. MySQL n'est accessible
depuis la machine hote que sur `127.0.0.1:3306`.

## 13. Variables de configuration

| Variable | Utilisation |
|---|---|
| `MYSQL_DATABASE` | nom de la base |
| `MYSQL_USER` | utilisateur applicatif |
| `MYSQL_PASSWORD` | mot de passe applicatif |
| `MYSQL_ROOT_PASSWORD` | mot de passe administrateur MySQL |
| `JWT_SECRET` | cle de signature des JWT |
| `JWT_EXPIRATION` | duree de validite en millisecondes |
| `CORS_ALLOWED_ORIGINS` | origines frontend autorisees |

Le fichier `.env` local est ignore par Git. Seul `.env.example`, sans secret
reel, sert de modele versionne.

## 14. Regles d'evolution

Pour conserver l'architecture actuelle :

1. Ajouter toute nouvelle route dans un controller de domaine.
2. Placer les regles metier et transactions dans un service.
3. Ne jamais retourner une entite JPA depuis un controller.
4. Utiliser un DTO et un mapper des que le modele public differe de l'entite.
5. Recuperer l'utilisateur courant depuis `Authentication`, jamais depuis un
   identifiant fourni par le client.
6. Ajouter toute evolution du schema dans une nouvelle migration Flyway.
7. Conserver les erreurs dans le contrat commun.
8. Documenter la route avec OpenAPI et ajouter les tests appropries.
9. Executer toute la suite dans Docker avant livraison.

## 15. Limites et extensions possibles

L'architecture actuelle couvre le backend demande. Les extensions suivantes
peuvent etre ajoutees sans modifier ses principes :

- refresh tokens et revocation ;
- roles et autorisations plus fines ;
- observabilite avec metriques et traces ;
- cache pour certaines lectures ;
- pipeline CI/CD ;
- profils Spring separes pour developpement et production ;
- deploiement sur une plateforme cloud.

Ces extensions doivent rester independantes des controllers et preserver
l'isolation des donnees par utilisateur.
