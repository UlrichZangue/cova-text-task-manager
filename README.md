# Task Manager API

Backend REST de gestion personnelle de taches construit avec Spring Boot 4,
Java 21, MySQL, JWT, Flyway et Docker Compose.

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

- Docker Engine avec Docker Compose v2 ;
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

## Routes

| Methode | Route | Auth | Description |
|---|---|---:|---|
| `POST` | `/api/auth/register` | non | Creer un utilisateur |
| `POST` | `/api/auth/login` | non | Obtenir un JWT |
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

La suite couvre 49 scenarios unitaires et d'integration. Les tests HTTP utilisent
MockMvc, le vrai contexte Spring et MySQL, avec rollback transactionnel.

## Architecture backend

```text
backend/src/main/java/com/taskmanager/backend
|-- auth/          inscription, connexion et DTO
|-- config/        Spring Security, CORS et OpenAPI
|-- exception/     contrat et gestion globale des erreurs
|-- security/      JWT, filtre et UserDetailsService
|-- task/          controleur, service, DTO, mapping et repository
`-- user/          entite et repository utilisateur
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
