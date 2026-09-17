# Plan de completion du backend Task Manager

Ce document transforme le cahier des charges en une feuille de route technique pour terminer l'API Spring Boot. Les etapes sont classees par dependance : chaque etape doit etre validee avant de passer a la suivante.

## 1. Etat actuel

### Deja disponible

- projet Spring Boot 4, Java 21 et Maven ;
- connexion MySQL avec JPA/Hibernate ;
- environnement Docker Compose (`backend` et `mysql`) ;
- entites `User` et `Task` ;
- enumerations `TaskStatus` et `TaskPriority` ;
- repositories `UserRepository` et `TaskRepository` ;
- inscription d'un utilisateur avec mot de passe BCrypt ;
- services JWT et filtre d'authentification ;
- configuration Spring Security stateless ;
- Swagger/OpenAPI accessible ;
- un test de chargement du contexte Spring.

### Reste a realiser en priorite

- validation complete de l'inscription ;
- endpoint de connexion et emission du JWT ;
- CRUD complet des taches ;
- recherche, filtres et pagination ;
- controle strict de la propriete des taches ;
- gestion uniforme des erreurs ;
- tests unitaires, API et securite ;
- durcissement de la configuration Docker et production.

## 2. Etape 1 - Stabiliser le modele et la base de donnees

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Travaux

1. Ajouter `createdAt` a l'entite `User` avec initialisation `@PrePersist`.
2. Conserver dans `Task` : `title`, `description`, `status`, `priority`, `dueDate`, `createdAt`, `updatedAt`, `deletedAt` et `user`.
3. Fixer des longueurs de colonnes coherentes, par exemple 150 caracteres pour le titre et 255 pour l'email.
4. Ajouter les index utiles sur `users.email`, `tasks.user_id`, `tasks.status`, `tasks.priority` et `tasks.deleted_at`.
5. Remplacer progressivement `ddl-auto=update` par des migrations Flyway, au minimum une migration initiale pour `users` et `tasks`.

### Validation

- le demarrage sur une base vide cree ou migre correctement le schema ;
- l'email est unique en base ;
- une tache ne peut pas exister sans utilisateur ;
- les dates systeme sont automatiquement renseignees.

## 3. Etape 2 - Finaliser l'inscription et les DTO

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Fichiers concernes

- `auth/dto/RegisterRequest.java`
- `auth/dto/LoginRequest.java`
- `auth/dto/AuthResponse.java`
- `auth/controller/AuthController.java`
- `auth/service/AuthService.java`

### Travaux

1. Ajouter `confirmPassword` dans `RegisterRequest`.
2. Ajouter les validations Bean Validation :
   - `name` : `@NotBlank`, taille maximale ;
   - `email` : `@NotBlank` et `@Email` ;
   - `password` : `@NotBlank` et `@Size(min = 8)` ;
   - `confirmPassword` : obligatoire.
3. Ajouter `@Valid` sur les corps de requete des controleurs.
4. Verifier dans `AuthService` que les deux mots de passe sont identiques.
5. Normaliser l'email avec `trim()` et `toLowerCase()` avant recherche et sauvegarde.
6. Remplacer les `RuntimeException` generiques par `BadRequestException` ou une exception metier dediee.
7. Ne jamais retourner une entite `User` directement depuis un controleur.

### Validation

- une inscription valide retourne `201 Created` ;
- un email invalide retourne `400 Bad Request` ;
- deux mots de passe differents retournent `400 Bad Request` ;
- un email deja utilise retourne `409 Conflict` ou `400 Bad Request`, selon la convention retenue ;
- aucun mot de passe ou hash n'apparait dans la reponse.

## 4. Etape 3 - Implementer la connexion JWT

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Travaux

1. Ajouter `POST /api/auth/login` dans `AuthController`.
2. Ajouter une methode `login(LoginRequest)` dans `AuthService`.
3. Authentifier l'email et le mot de passe avec `AuthenticationManager` et `UsernamePasswordAuthenticationToken`.
4. Generer le token avec `JwtService` apres une authentification reussie.
5. Retourner un `AuthResponse` contenant au minimum :
   - `token` ;
   - `tokenType: Bearer` ;
   - `expiresIn` si possible.
6. Retourner `401 Unauthorized` pour des identifiants incorrects.
7. Configurer OpenAPI avec un schema de securite HTTP Bearer JWT.

### Validation

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

- la reponse contient un JWT valide ;
- une route privee accepte `Authorization: Bearer <token>` ;
- un token absent, invalide ou expire produit une reponse `401` structuree ;
- l'API n'utilise ni session HTTP ni authentification Basic.

## 5. Etape 4 - Uniformiser les erreurs

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Fichiers concernes

- `exception/ErrorResponse.java`
- `exception/GlobalExceptionHandler.java`
- `exception/BadRequestException.java`
- `exception/ResourceNotFoundException.java`

### Travaux

1. Implementer les classes d'exception avec constructeurs et messages.
2. Definir un format stable : `timestamp`, `status`, `error`, `message`, `path` et, pour la validation, `fieldErrors`.
3. Gerer au minimum :
   - `MethodArgumentNotValidException` -> `400` ;
   - `BadRequestException` -> `400` ;
   - identifiants invalides -> `401` ;
   - acces a une ressource interdite -> `403` ;
   - `ResourceNotFoundException` -> `404` ;
   - email duplique -> `409` ;
   - erreur inattendue -> `500` sans exposer la stack trace.
4. Ajouter des handlers JSON pour les erreurs Spring Security produites avant le controleur.

### Validation

Toutes les erreurs de l'API doivent avoir le meme format JSON et le bon code HTTP.

Validation realisee avec 15 tests automatises et des appels HTTP sur le conteneur
reconstruit : `400`, `401`, `403`, `404` et `405` utilisent le contrat commun.

## 6. Etape 5 - Creer les DTO et le mapping des taches

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Fichiers a creer

```text
task/dto/TaskRequest.java
task/dto/TaskResponse.java
task/mapper/TaskMapper.java
```

### Contenu recommande

`TaskRequest` :

- `title` obligatoire, entre 1 et 150 caracteres ;
- `description` facultative ;
- `status` facultatif a la creation, valeur par defaut `TODO` ;
- `priority` facultative, valeur par defaut `MEDIUM` ;
- `dueDate` facultative.

`TaskResponse` :

- `id`, `title`, `description`, `status`, `priority`, `dueDate`, `createdAt`, `updatedAt` ;
- ne pas exposer l'entite `User`, son email ou son mot de passe.

### Validation

- les controleurs ne recoivent et ne retournent aucune entite JPA ;
- un titre vide ou trop long retourne `400` ;
- les valeurs d'enumeration invalides retournent une erreur explicite.

Le DTO de sortie n'expose ni l'utilisateur ni les metadonnees de suppression. Le
mapper applique `TODO` et `MEDIUM` a la creation, et preserve ces valeurs lors
d'une mise a jour lorsqu'elles sont omises.

## 7. Etape 6 - Implementer le CRUD securise des taches

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Fichiers a creer

```text
task/service/TaskService.java
task/controller/TaskController.java
```

### Routes obligatoires

| Methode | Route | Resultat attendu |
|---|---|---|
| `POST` | `/api/tasks` | creer une tache pour l'utilisateur connecte, `201` |
| `GET` | `/api/tasks` | lister uniquement ses taches actives, `200` |
| `GET` | `/api/tasks/{id}` | obtenir une de ses taches, `200` ou `404` |
| `PUT` | `/api/tasks/{id}` | modifier une de ses taches, `200` |
| `DELETE` | `/api/tasks/{id}` | supprimer une de ses taches, `204` |

### Regles de securite

1. Recuperer l'utilisateur connecte depuis le `SecurityContext`, jamais depuis un `userId` fourni par le client.
2. Toujours charger une tache avec son proprietaire, par exemple `findByIdAndUserAndDeletedAtIsNull`.
3. Ne jamais permettre la lecture, modification ou suppression de la tache d'un autre utilisateur.
4. Choisir une strategie de suppression coherente :
   - suppression logique en renseignant `deletedAt`, deja prevu dans l'entite ;
   - ou suppression physique en retirant `deletedAt` du modele.
5. Avec la suppression logique, toutes les recherches doivent exclure `deletedAt IS NOT NULL`.

### Validation

- deux utilisateurs ne voient jamais les taches l'un de l'autre ;
- un identifiant inconnu retourne `404` ;
- une tache supprimee ne reapparait pas dans la liste ni dans la recherche.

Validation realisee avec 29 tests automatises et un scenario HTTP complet a deux
utilisateurs : creation `201`, lecture et modification `200`, acces croise `404`,
suppression `204`, puis lecture de la tache supprimee `404`.

## 8. Etape 7 - Ajouter recherche, filtres, tri et pagination

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Contrat recommande

```http
GET /api/tasks?search=rapport&status=TODO&priority=HIGH&page=0&size=10&sort=createdAt,desc
```

### Travaux

1. Faire retourner un `Page<TaskResponse>` ou un DTO de page stable.
2. Etendre `TaskRepository` avec `JpaSpecificationExecutor<Task>` ou des requetes JPA clairement nommees.
3. Appliquer obligatoirement les criteres `user = utilisateur connecte` et `deletedAt IS NULL`.
4. Rendre `search`, `status` et `priority` facultatifs et combinables.
5. Rechercher au minimum dans le titre, sans tenir compte de la casse ; la description peut aussi etre incluse.
6. Limiter `size` a une valeur raisonnable, par exemple 100 maximum.
7. Autoriser uniquement une liste blanche de champs de tri.

### Validation

- chaque filtre fonctionne seul ;
- recherche et filtres fonctionnent ensemble ;
- les metadonnees `page`, `size`, `totalElements` et `totalPages` sont exactes ;
- aucune recherche ne retourne les taches d'un autre utilisateur.

Validation realisee avec 30 tests automatises et un scenario HTTP sur MySQL :
recherche insensible a la casse dans le titre et la description, filtres combines,
pagination, tri en liste blanche, limite de 100 elements et isolation utilisateur.

## 9. Etape 8 - Ajouter les statistiques du dashboard

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Route recommandee

```http
GET /api/tasks/stats
```

### Reponse

```json
{
  "total": 24,
  "todo": 8,
  "inProgress": 6,
  "done": 10
}
```

Les comptes doivent etre filtres par utilisateur et ignorer les taches supprimees.

Validation realisee avec 32 tests automatises et un scenario HTTP sur MySQL :
comptage par statut, exclusion des taches supprimees, isolation entre utilisateurs,
reponse a zero pour un compte vide et protection JWT de la route.

## 10. Etape 9 - Completer la configuration de securite

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Travaux

1. Garder publiques uniquement les routes d'authentification et la documentation necessaire.
2. Verifier le comportement du filtre JWT lorsque l'en-tete est absent ou mal forme.
3. Ajouter une configuration CORS explicite pour l'URL du frontend, parametrable par variable d'environnement.
4. Ne pas placer le secret JWT en dur dans `docker-compose.yml` ; le lire depuis `.env` ou un secret Docker/GCP.
5. Utiliser un secret aleatoire suffisamment long et refuser le demarrage si le secret est absent ou trop court.
6. Ne jamais journaliser le JWT, les mots de passe ou les donnees sensibles.
7. Prevoir une expiration courte et, en bonus, un mecanisme de refresh token.

### Point d'attention cookie

Le cahier des charges mentionne a la fois un cookie HttpOnly et l'en-tete `Authorization`. Pour une API commune web/mobile, retenir et documenter une convention :

- mobile : token Bearer dans l'en-tete ;
- web : cookie `HttpOnly`, `Secure`, `SameSite` gere par le backend, ou Bearer si ce choix est explicitement assume.

Convention retenue pour cette API : JWT Bearer dans l'en-tete `Authorization` pour
les clients web et mobile. Aucun cookie d'authentification ni aucune session HTTP
n'est utilise ; CORS n'autorise donc pas les credentials et limite explicitement
les origines, methodes et en-tetes acceptes.

Validation realisee avec 39 tests automatises et des controles HTTP : origine CORS
autorisee et origine inconnue, en-tete absent, schema Basic, JWT invalide, route
publique, secret court ou absent et echec explicite de Compose sans `JWT_SECRET`.

## 11. Etape 10 - Construire une vraie suite de tests

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Tests unitaires

- `AuthServiceTest` : inscription, email duplique, confirmation invalide, login valide et invalide ;
- `JwtServiceTest` : generation, lecture du sujet, expiration et token invalide ;
- `TaskServiceTest` : creation, lecture, modification, suppression et controle du proprietaire.

### Tests API/integration

- `AuthControllerTest` : validations et codes HTTP de register/login ;
- `TaskControllerTest` : toutes les routes, pagination et filtres ;
- tests de securite : sans token, token invalide, token expire, utilisateur A contre tache de B ;
- test du format uniforme des erreurs.

### Base de test

Utiliser de preference Testcontainers avec MySQL afin de tester le meme moteur qu'en production. Les tests doivent etre independants, reproductibles et nettoyer leurs donnees.

### Objectif minimum

- couvrir tous les chemins metier critiques ;
- avoir au moins un test positif et un test negatif par route ;
- ne plus se limiter au seul `contextLoads()`.

La suite compte 48 tests. Les tests d'integration MockMvc utilisent le vrai contexte
Spring et MySQL dans Docker, avec rollback transactionnel. Ils couvrent inscription,
connexion, CRUD, propriete des taches, suppression logique, recherche, filtres,
pagination, statistiques, erreurs structurees, CORS et JWT absent, invalide ou expire.
Une verification apres execution confirme qu'aucune donnee de test ne reste en base.

## 12. Etape 11 - Documenter l'API

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

1. Ajouter titres, descriptions, exemples et codes de reponse OpenAPI.
2. Declarer l'authentification Bearer sur les routes `/api/tasks/**`.
3. Documenter les parametres de recherche, filtre, pagination et tri.
4. Ajouter dans le `README.md` : prerequis, variables d'environnement, commandes Docker, URL Swagger et exemples `curl`.
5. Fournir un fichier `.env.example` sans secret reel.

Le `README.md` racine documente les prerequis, variables, commandes Docker,
architecture, securite, erreurs, tests et exemples `curl`. OpenAPI decrit les
routes d'authentification et de taches, leurs parametres, codes de reponse,
exemples de DTO et le schema JWT Bearer. Le contrat est verrouille par un test
d'integration et la suite complete compte 49 tests reussis.

## 13. Etape 12 - Finaliser Docker et l'automatisation

**Statut : terminee et validee avec Docker le 17 septembre 2026.**

### Ameliorations Docker

1. Executer les tests pendant la phase de construction ou dans un service Docker Compose dedie `test`.
2. Conserver le build multi-stage et executer le runtime avec un utilisateur non-root.
3. Ajouter un healthcheck HTTP au backend.
4. Ajouter `SPRING_PROFILES_ACTIVE` et des profils `dev`, `test` et `prod`.
5. Retirer l'exposition publique du port MySQL lorsqu'elle n'est pas necessaire.
6. Ajouter des limites et options de logs si l'environnement cible le requiert.

La livraison conserve le build multi-stage, execute l'application avec un
utilisateur non-root, expose un healthcheck HTTP Actuator et fournit un service
Compose `test`. Le port MySQL est lie uniquement a `127.0.0.1`. Les options de
logs et profils Spring restent a adapter a l'infrastructure cible, car elles ne
sont pas necessaires au deploiement Docker local demande.

### Commandes de verification

```bash
# Reconstruire le backend
docker compose build --no-cache backend

# Demarrer MySQL et l'API
docker compose up -d

# Verifier les conteneurs et les journaux
docker compose ps
docker compose logs --tail=100 backend

# Executer les tests dans le service Compose dedie
docker compose --profile test run --rm test
```

## 14. Ordre de livraison conseille

### Lot 1 - MVP obligatoire

- modele stabilise ;
- inscription validee ;
- connexion JWT ;
- erreurs uniformes ;
- CRUD des taches securise ;
- recherche par titre et filtre par statut ;
- tests critiques ;
- Swagger et Docker fonctionnels.

### Lot 2 - Qualite recommandee

- priorite et date d'echeance ;
- pagination et tri ;
- statistiques du dashboard ;
- Flyway et Testcontainers ;
- CORS parametrable ;
- documentation complete.

### Lot 3 - Bonus production

- CI/CD ;
- deploiement GCP ;
- observabilite et healthchecks avances ;
- refresh token/revocation ;
- application Flutter consommant la meme API.

## 15. Definition de termine

Le backend est considere termine lorsque :

- toutes les routes obligatoires du cahier des charges sont implementees ;
- un utilisateur ne peut acceder qu'a ses propres taches ;
- inscription et connexion sont validees et securisees ;
- les erreurs sont explicites et homogenes ;
- recherche et filtre par statut fonctionnent ;
- la suite de tests passe integralement dans Docker ;
- Swagger decrit les contrats reels de l'API ;
- `docker compose up -d` permet de lancer une instance fonctionnelle sans modification manuelle du code.
