# Task Manager Mobile

Application Flutter Android/iOS consommant la même API Spring Boot que le
frontend web.

## Fonctionnalités

- inscription et connexion JWT ;
- conservation chiffrée du token avec `flutter_secure_storage` ;
- recherche et filtrage des tâches par statut ;
- création, modification et suppression ;
- changement rapide de statut ;
- thème clair ou sombre selon le système ;
- états de chargement, d'erreur et de liste vide.

## Architecture

```text
lib/
├── core/                 # configuration, HTTP et stockage sécurisé
├── features/
│   ├── auth/             # dépôt, contrôleur et écrans d'authentification
│   └── tasks/            # modèle, dépôt et écrans des tâches
├── shared/               # thème partagé
├── app.dart
└── main.dart
```

Les dépôts isolent les appels Dio des widgets. `AuthController` porte la session
et le JWT est injecté automatiquement dans l'en-tête `Authorization`.

## Démarrage

1. Démarrer l'API depuis la racine du monorepo :

   ```bash
   docker compose up -d --build
   ```

2. Installer les dépendances :

   ```bash
   cd mobile
   flutter pub get
   ```

3. Lancer un émulateur Android puis l'application :

   ```bash
   flutter run
   ```

Par défaut, Android utilise `http://10.0.2.2:8080`, l'adresse de la machine hôte
vue depuis l'émulateur. Pour un appareil physique, utiliser l'adresse IP locale
du poste :

```bash
flutter run --dart-define=API_URL=http://192.168.1.20:8080
```

Pour le simulateur iOS :

```bash
flutter run --dart-define=API_URL=http://localhost:8080
```

## Qualité

```bash
dart format --set-exit-if-changed lib test
flutter analyze
flutter test
```
