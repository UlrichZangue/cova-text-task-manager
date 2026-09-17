# Task Manager API

Backend REST de gestion personnelle de taches construit avec Spring Boot 4,
Java 21, MySQL, JWT, Flyway et Docker Compose.

La description detaillee des couches et des flux se trouve dans
[`ARCHITECTURE_BACKEND.md`](ARCHITECTURE_BACKEND.md).

## Demarrage rapide apres un clone

Cette procedure suffit pour evaluer le backend sur une machine qui possede
Docker. Java, Maven et MySQL ne doivent pas etre installes localement.

```bash
git clone https://github.com/UlrichZangue/cova-text-task-manager.git
cd cova-text-task-manager
cp .env.example .env
docker compose up -d --build
docker compose ps
```

Le premier build peut prendre quelques minutes, le temps de telecharger les
images et dependances. Attendre que `backend` et `mysql` affichent l'etat
`healthy`, puis verifier l'API :

```bash
curl http://localhost:8080/actuator/health
```

Resultat attendu :

```json
{"status":"UP"}
```

Swagger permet ensuite de tester toutes les routes depuis le navigateur :

<http://localhost:8080/swagger-ui.html>

Executer toute la suite de tests dans Docker :

```bash
docker compose --profile test run --rm test
```

Le resultat attendu est `BUILD SUCCESS` avec `51` tests, aucun echec et aucune
erreur. Le service `test` attend automatiquement que MySQL soit sain. L'option
`--rm` supprime le conteneur de test apres son execution.

Les valeurs de `.env.example` sont uniquement destinees a une evaluation
locale. Elles permettent un premier demarrage immediat. Pour conserver ou
deployer l'application, remplacer les mots de passe et generer un vrai secret
JWT avec `openssl rand -base64 48`.

## Fonctionnalites

- inscription et connexion avec JWT Bearer ;
- CRUD securise des taches par utilisateur ;
- suppression logique ;
- recherche dans le titre et la description ;
- filtres par statut et priorite ;
- pagination et tri en liste blanche ;
- statistiques du dashboard ;
- erreurs JSON uniformes ;
- documentation OpenAPI/Swagger ;
- tests unitaires, integration HTTP et securite sur MySQL.

## Prerequis

- Git ;
- Docker Engine avec Docker Compose v2, ou Docker Desktop ;
- `curl` et `jq` pour executer les exemples ;
- `openssl` pour generer un secret JWT.

Java et Maven ne sont pas requis sur la machine hote pour le demarrage ou les
tests presentes ci-dessous.

## Configuration

Creer le fichier local de configuration depuis le modele :

```bash
cp .env.example .env
```

Generer ensuite un secret JWT et remplacer la valeur `JWT_SECRET` dans `.env` :

```bash
openssl rand -base64 48
```

| Variable | Obligatoire | Description |
|---|---:|---|
| `MYSQL_DATABASE` | oui | Base MySQL utilisee par l'API |
| `MYSQL_USER` | oui | Utilisateur applicatif MySQL |
| `MYSQL_PASSWORD` | oui | Mot de passe de l'utilisateur MySQL |
| `MYSQL_ROOT_PASSWORD` | oui | Mot de passe administrateur MySQL |
| `JWT_SECRET` | oui | Secret aleatoire d'au moins 32 octets |
| `JWT_EXPIRATION` | non | Duree du token en millisecondes, `900000` par defaut Compose |
| `CORS_ALLOWED_ORIGINS` | non | Origines autorisees, separees par des virgules |

Le fichier `.env` est ignore par Git. `.env.example` ne contient aucun secret
reel et peut etre versionne.

## Demarrage Docker

```bash
docker compose up -d --build
docker compose ps
```

Services disponibles :

- API : <http://localhost:8080>
- Swagger UI : <http://localhost:8080/swagger-ui.html>
- contrat OpenAPI JSON : <http://localhost:8080/v3/api-docs>
- sante de l'API : <http://localhost:8080/actuator/health>
- MySQL : `127.0.0.1:3306`

Consulter les journaux ou arreter l'environnement :

```bash
docker compose logs -f backend
docker compose down
```

Les donnees MySQL sont conservees dans le volume `mysql_data`. La commande
`docker compose down -v` supprime aussi ce volume et toutes ses donnees.

## Authentification

L'API utilise exclusivement un JWT Bearer dans l'en-tete `Authorization`. Elle
n'utilise ni session HTTP, ni cookie d'authentification, ni Basic Auth.

Inscription :

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Alice Example",
    "email": "alice@example.com",
    "password": "Password123!",
    "confirmPassword": "Password123!"
  }'
```

Connexion et extraction du token :

```bash
TOKEN=$(curl -sS -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "alice@example.com",
    "password": "Password123!"
  }' | jq -r '.token')
```

Recuperer le profil associe au token :

```bash
curl -sS http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

## Routes

| Methode | Route | Auth | Description |
|---|---|---:|---|
| `POST` | `/api/auth/register` | non | Creer un utilisateur |
| `POST` | `/api/auth/login` | non | Obtenir un JWT |
| `GET` | `/api/users/me` | oui | Recuperer le profil connecte |
| `POST` | `/api/tasks` | oui | Creer une tache |
| `GET` | `/api/tasks` | oui | Rechercher et paginer les taches |
| `GET` | `/api/tasks/stats` | oui | Obtenir les compteurs du dashboard |
| `GET` | `/api/tasks/{id}` | oui | Consulter une tache |
| `PUT` | `/api/tasks/{id}` | oui | Modifier une tache |
| `DELETE` | `/api/tasks/{id}` | oui | Supprimer logiquement une tache |

Une tache inconnue, supprimee ou appartenant a un autre utilisateur retourne
`404`. Le client ne fournit jamais de `userId` : le proprietaire vient du JWT.

## Exemples taches

Creer une tache :

```bash
curl -i -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Prepare quarterly report",
    "description": "Compile the finance figures",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-10-01"
  }'
```

Rechercher, filtrer, paginer et trier :

```bash
curl -sS --get http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  --data-urlencode 'search=report' \
  --data-urlencode 'status=TODO' \
  --data-urlencode 'priority=HIGH' \
  --data-urlencode 'page=0' \
  --data-urlencode 'size=10' \
  --data-urlencode 'sort=createdAt,desc' | jq
```

`size` doit etre compris entre 1 et 100. Les champs de tri autorises sont
`id`, `title`, `status`, `priority`, `dueDate`, `createdAt` et `updatedAt`.
La direction accepte `asc` ou `desc`.

Modifier et supprimer une tache :

```bash
TASK_ID='550e8400-e29b-41d4-a716-446655440000'

curl -i -X PUT "http://localhost:8080/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Report completed","status":"DONE","priority":"MEDIUM"}'

curl -i -X DELETE "http://localhost:8080/api/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Statistiques :

```bash
curl -sS http://localhost:8080/api/tasks/stats \
  -H "Authorization: Bearer $TOKEN" | jq
```

```json
{
  "total": 24,
  "todo": 8,
  "inProgress": 6,
  "done": 10
}
```

## Pagination

La liste retourne un contrat stable :

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

## Erreurs

Toutes les erreurs applicatives et de securite suivent la meme structure :

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Task not found",
  "path": "/api/tasks/550e8400-e29b-41d4-a716-446655440000",
  "fieldErrors": {},
  "timestamp": "2026-09-17T18:00:00Z"
}
```

Codes principaux : `400`, `401`, `403`, `404`, `405`, `409` et `500`.

## Tests Docker

Le profil Compose `test` construit l'etage Maven, attend que MySQL soit sain,
puis execute toute la suite :

```bash
docker compose --profile test run --rm test
```

Resultat attendu :

```text
Tests run: 51, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

La suite couvre 51 scenarios unitaires et d'integration. Les tests HTTP utilisent
MockMvc, le vrai contexte Spring et MySQL, avec rollback transactionnel. Il n'est
pas necessaire d'installer Maven sur la machine hote.

## Architecture backend

```text
backend/src/main/java/com/taskmanager/backend
|-- auth/          inscription, connexion et DTO
|-- config/        Spring Security, CORS et OpenAPI
|-- exception/     contrat et gestion globale des erreurs
|-- security/      JWT, filtre et UserDetailsService
|-- task/          controleur, service, DTO, mapping et repository
`-- user/          profil, service, DTO, entite et repository
```

Flyway gere le schema dans `backend/src/main/resources/db/migration`. Hibernate
utilise `ddl-auto=validate` et ne modifie pas automatiquement la base.

## Securite

- mots de passe hashes avec BCrypt ;
- secret JWT obligatoire et controle a 32 octets minimum ;
- expiration configurable ;
- CORS limite aux origines declarees ;
- acces aux taches toujours filtre par proprietaire ;
- aucune stack trace retournee au client ;
- variables sensibles conservees hors Git.
- conteneur applicatif execute avec un utilisateur non-root ;
- endpoint de sante public limite a `/actuator/health`.

Pour un deploiement, utiliser un gestionnaire de secrets, des mots de passe MySQL
uniques, HTTPS et une origine CORS correspondant exactement au frontend.

## Depannage

Afficher les journaux si un service ne devient pas `healthy` :

```bash
docker compose logs --tail=200 backend mysql
```

Si les ports `8080` ou `3306` sont deja utilises, arreter le service local qui
les occupe ou modifier le port hote correspondant dans `docker-compose.yml`.

Recreer une installation locale complete, y compris la base de donnees :

```bash
docker compose down -v
docker compose up -d --build
```

Attention : l'option `-v` supprime definitivement les donnees du volume MySQL.
