#!/usr/bin/env bash

set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
DEMO_NAME="${DEMO_NAME:-Camille Demo}"
DEMO_EMAIL="${DEMO_EMAIL:-demo@taskmanager.local}"
DEMO_PASSWORD="${DEMO_PASSWORD:-Demo123!}"

for command in curl jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "Erreur: la commande '$command' est requise." >&2
    exit 1
  fi
done

echo "Verification de l'API sur $API_URL..."
curl --fail --silent --show-error "$API_URL/actuator/health" >/dev/null

register_payload=$(jq -nc \
  --arg name "$DEMO_NAME" \
  --arg email "$DEMO_EMAIL" \
  --arg password "$DEMO_PASSWORD" \
  '{name: $name, email: $email, password: $password, confirmPassword: $password}')

register_status=$(curl --silent --output /dev/null --write-out '%{http_code}' \
  --request POST "$API_URL/api/auth/register" \
  --header 'Content-Type: application/json' \
  --data "$register_payload")

if [[ "$register_status" != "201" && "$register_status" != "409" ]]; then
  echo "Erreur: inscription du compte demo impossible (HTTP $register_status)." >&2
  exit 1
fi

login_payload=$(jq -nc \
  --arg email "$DEMO_EMAIL" \
  --arg password "$DEMO_PASSWORD" \
  '{email: $email, password: $password}')

token=$(curl --fail --silent --show-error \
  --request POST "$API_URL/api/auth/login" \
  --header 'Content-Type: application/json' \
  --data "$login_payload" | jq -er '.token')

existing_titles=$(curl --fail --silent --show-error --get "$API_URL/api/tasks" \
  --header "Authorization: Bearer $token" \
  --data-urlencode 'page=0' \
  --data-urlencode 'size=100' \
  --data-urlencode 'sort=createdAt,desc' | jq -c '[.content[].title]')

tasks=(
  "Definir les objectifs du sprint|Clarifier les resultats attendus avec l'equipe.|TODO|HIGH|2026-09-21"
  "Preparer la maquette mobile|Finaliser les ecrans principaux pour la revue design.|TODO|MEDIUM|2026-09-23"
  "Verifier les textes de l'interface|Relire les libelles, messages vides et erreurs.|TODO|LOW|2026-09-25"
  "Planifier les entretiens utilisateurs|Contacter cinq utilisateurs pour la prochaine session.|TODO|MEDIUM|2026-09-28"
  "Implementer le tableau Kanban|Ajouter le glisser-deposer et les etats optimistes.|IN_PROGRESS|HIGH|2026-09-19"
  "Documenter le parcours Docker|Verifier les commandes sur une installation vierge.|IN_PROGRESS|MEDIUM|2026-09-20"
  "Analyser les retours de recette|Regrouper les anomalies et definir leur priorite.|IN_PROGRESS|HIGH|2026-09-22"
  "Optimiser les requetes du dashboard|Mesurer les temps de reponse des statistiques.|IN_PROGRESS|LOW|2026-09-26"
  "Configurer l'authentification JWT|Securiser les routes et la gestion des erreurs 401.|DONE|HIGH|2026-09-12"
  "Ajouter les tests d'integration|Couvrir les principaux parcours API et securite.|DONE|HIGH|2026-09-14"
  "Finaliser le profil utilisateur|Afficher les informations du compte connecte.|DONE|MEDIUM|2026-09-16"
  "Relire le README|Valider le demarrage, les tests et les URLs publiques.|DONE|LOW|2026-09-17"
)

created=0
skipped=0

for task in "${tasks[@]}"; do
  IFS='|' read -r title description status priority due_date <<<"$task"

  if jq -e --arg title "$title" 'index($title) != null' <<<"$existing_titles" >/dev/null; then
    ((skipped += 1))
    continue
  fi

  payload=$(jq -nc \
    --arg title "$title" \
    --arg description "$description" \
    --arg status "$status" \
    --arg priority "$priority" \
    --arg dueDate "$due_date" \
    '{title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate}')

  curl --fail --silent --show-error \
    --request POST "$API_URL/api/tasks" \
    --header "Authorization: Bearer $token" \
    --header 'Content-Type: application/json' \
    --data "$payload" >/dev/null

  ((created += 1))
done

echo "Jeu de demonstration pret: $created tache(s) creee(s), $skipped deja presente(s)."
echo "Connexion: $DEMO_EMAIL / $DEMO_PASSWORD"
