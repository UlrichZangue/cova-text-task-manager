# Architecture frontend

## Vue d'ensemble

Le frontend suit une architecture **Feature First** inspiree de la Clean
Architecture. Le cahier des charges impose React, Vite et TypeScript ; les
principes de l'architecture de reference ont ete adaptes a cette stack.

```text
src/
|-- app/            routage, shell et composition des providers
|-- config/         lecture validee de la configuration
|-- core/           client HTTP, erreurs et stockage du token
|-- features/
|   |-- authentication/
|   |-- dashboard/
|   |-- tasks/
|   `-- users/
|-- shared/         composants, providers et utilitaires transverses
|-- styles/         design system et responsive
`-- test/           configuration Vitest
```

## Couches d'une feature

- `domain` contient les types metier et les contrats de repository. Il ne
  depend ni de React ni du transport HTTP.
- `application` contient les cas d'utilisation et assemble les contrats du
  domaine.
- `infrastructure` implemente ces contrats avec le client HTTP.
- `presentation` contient les pages, composants, schemas de formulaires et
  hooks TanStack Query.

La direction des dependances est la suivante :

```text
presentation -> application -> domain
infrastructure -------------> domain
```

`application/*.dependencies.ts` joue le role de composition locale et fournit
les implementations HTTP aux cas d'utilisation.

## Etat et donnees

TanStack Query est la source de verite des donnees serveur. Les mutations
invalident les cles concernees apres succes. L'etat local est limite aux
formulaires, filtres, modales, menu mobile et theme. Aucun objet metier provenant
de l'API n'est duplique dans un store global.

Le client HTTP centralise l'URL de base, la serialisation JSON, le Bearer token
et la conversion du contrat d'erreur backend en `ApiError`. Une reponse `401`
efface la session et declenche le retour vers la connexion.

## Authentification

L'API emet un JWT dans le corps de la reponse et attend un header
`Authorization: Bearer`. Le navigateur le conserve donc dans `localStorage`.
Un cookie `HttpOnly` ne peut pas etre cree par JavaScript : cette evolution
necessiterait une route backend qui pose elle-meme le cookie et une protection
CSRF adaptee.

Les routes applicatives sont protegees par `ProtectedRoute`. `AuthProvider`
restaure la session au chargement et coordonne connexion, inscription et
deconnexion.

## Routage

- `/login` et `/register` : routes publiques ;
- `/dashboard` : statistiques et activite recente ;
- `/tasks` : recherche, filtres, pagination et CRUD ;
- `/profile` : profil de l'utilisateur connecte ;
- `/` redirige vers `/dashboard`.

## Verification

Avant une livraison :

```bash
npm run format:check
npm run lint
npm test
npm run build
```

La livraison Nginx utilise un fallback vers `index.html` afin que les routes de
la SPA fonctionnent aussi lors d'un chargement direct.
