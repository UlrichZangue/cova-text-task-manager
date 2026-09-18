# Task Manager Web

Interface React et TypeScript du Task Manager. Elle couvre l'inscription, la
connexion, le tableau de bord, le profil et le CRUD complet des taches avec
recherche, filtres, tri et pagination.

## Demarrage recommande avec Docker

Depuis la racine du depot :

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

L'application est disponible sur <http://localhost:3000> et l'API sur
<http://localhost:8080>. Les trois services doivent etre `healthy`.

## Developpement local

Prerequis : Node.js 20+ et npm. Le backend doit etre accessible sur le port
configure dans `VITE_API_URL`.

```bash
cp .env.example .env
npm ci
npm run dev
```

Vite affiche l'URL locale, par defaut <http://localhost:5173>. Si cette origine
est utilisee, ajouter `http://localhost:5173` a `CORS_ALLOWED_ORIGINS` dans le
fichier `.env` racine avant de relancer le backend.

## Commandes de qualite

```bash
npm run format:check
npm run lint
npm test
npm run build
```

`npm test` execute les tests Vitest une seule fois. `npm run test:watch` les
relance pendant le developpement. Le build produit les fichiers statiques dans
`dist/`.

Prettier formalise la mise en forme du code. Utiliser `npm run format` pour
appliquer automatiquement les conventions, puis `npm run format:check` dans les
controles locaux ou la CI.

Sans Node.js local, depuis la racine du depot :

```bash
docker compose --profile test run --rm --build frontend-test
```

## Configuration

| Variable       | Defaut                  | Description           |
| -------------- | ----------------------- | --------------------- |
| `VITE_API_URL` | `http://localhost:8080` | URL publique de l'API |

Les variables Vite sont injectees au build. Une modification de
`VITE_API_URL` exige donc une reconstruction de l'image frontend.

## Stack

- React 19, TypeScript et Vite ;
- React Router pour les routes publiques et protegees ;
- TanStack Query pour l'etat serveur et l'invalidation du cache ;
- React Hook Form et Zod pour les formulaires ;
- Lucide React pour les icones ;
- Prettier et ESLint pour une mise en forme et une qualite de code reproductibles ;
- Vitest et Testing Library pour les tests.

Les choix de structure, les flux et les conventions adaptees au projet sont
detailles dans [`ARCHITECTURE.md`](ARCHITECTURE.md).
