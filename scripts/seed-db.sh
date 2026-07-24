#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

DB_USER="${DB_USERNAME:-prestamos}"
DB_NAME="${DB_NAME:-prestamos_facil}"

echo "==> Cleaning and re-seeding database '$DB_NAME'..."

if docker compose exec -T postgres pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; then
  docker compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" < "$ROOT_DIR/scripts/seed.sql"
  echo "==> Database successfully cleaned and re-seeded!"
else
  echo "Error: PostgreSQL container is not running. Please start the environment with 'make start' or 'make dev'."
  exit 1
fi
