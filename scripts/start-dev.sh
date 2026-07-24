#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# --- Load .env if present ---
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
elif [ -z "${DB_PASSWORD:-}" ]; then
  echo "No .env found and DB_PASSWORD is not set."
  echo "Run 'scripts/generate-dev-env.sh' to create one."
  exit 1
fi

# --- Defaults ---
HTTP_PORT="${HTTP_PORT:-4010}"
HTTP_PORT_FALLBACK="${HTTP_PORT_FALLBACK:-4011}"
FRONTEND_PORT="${FRONTEND_PORT:-4000}"
FRONTEND_PORT_FALLBACK="${FRONTEND_PORT_FALLBACK:-4001}"

cleanup() {
  echo ""
  echo "Shutting down..."
  if [ -n "${BACKEND_PID:-}" ]; then
    kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
  fi
  docker compose down
  echo "Done."
  exit 0
}
trap cleanup SIGINT SIGTERM

# --- Helper: find available port ---
find_port() {
  local primary="$1"
  local fallback="$2"
  if ! lsof -ti :"$primary" > /dev/null 2>&1; then
    echo "$primary"
    return
  fi
  echo "Port $primary is in use, trying fallback $fallback..." >&2
  if ! lsof -ti :"$fallback" > /dev/null 2>&1; then
    echo "$fallback"
    return
  fi
  echo "Fallback $fallback also in use. Killing process on $primary..." >&2
  kill -9 "$(lsof -ti :"$primary")" 2>/dev/null || true
  sleep 1
  echo "$primary"
}

# --- Step 1: Start PostgreSQL ---
echo "==> Starting PostgreSQL..."
docker compose up -d

echo "==> Waiting for PostgreSQL to be healthy..."
until docker compose exec -T postgres pg_isready -U "${DB_USERNAME:-prestamos}" -d "${DB_NAME:-prestamos_facil}" > /dev/null 2>&1; do
  sleep 2
done
echo "PostgreSQL is healthy."

# --- Step 2: Find backend port and start backend ---
BACKEND_PORT=$(find_port "$HTTP_PORT" "$HTTP_PORT_FALLBACK")
echo "==> Starting backend on port $BACKEND_PORT..."
cd apps/backend
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun --args="--server.port=$BACKEND_PORT" &
BACKEND_PID=$!
cd "$ROOT_DIR"

# --- Step 3: Find frontend port and start frontend ---
FRONTEND_PORT_ACTUAL=$(find_port "$FRONTEND_PORT" "$FRONTEND_PORT_FALLBACK")
echo "==> Starting frontend on port $FRONTEND_PORT_ACTUAL..."
cd apps/frontend
pnpm dev --port "$FRONTEND_PORT_ACTUAL"
cd "$ROOT_DIR"
