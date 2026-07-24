#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  echo "Error: .env already exists. Delete it first if you want to regenerate."
  exit 1
fi

DB_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 32)

cat > "$ENV_FILE" << EOF
# --- Database ---
DB_HOST=localhost
DB_PORT=5432
DB_NAME=prestamos_facil
DB_USERNAME=prestamos
DB_PASSWORD=${DB_PASSWORD}

# --- Backend ---
HTTP_PORT=4010
HTTP_PORT_FALLBACK=4011
SPRING_PROFILES_ACTIVE=dev

# --- Auth / JWT ---
JWT_SECRET=${JWT_SECRET}

# --- Email (Mailpit for dev) ---
MAIL_USERNAME=
MAIL_PASSWORD=

# --- Frontend ---
FRONTEND_PORT=4000
FRONTEND_PORT_FALLBACK=4001
PUBLIC_API_BASE_URL=http://localhost:4010/api/v1
FRONTEND_ORIGIN=http://localhost:4000
EOF

echo "Generated $ENV_FILE with random secrets."
echo "  - DB_PASSWORD: ${DB_PASSWORD}"
echo "  - JWT_SECRET: ${JWT_SECRET}"
