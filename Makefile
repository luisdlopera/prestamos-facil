-include .env
export

.PHONY: dev api start stop down db-seed backend backend-test backend-check frontend-install frontend frontend-check frontend-test frontend-build frontend-lint frontend-format test clean

dev:
	@echo "Starting development environment..."
	@bash scripts/start-dev.sh

start:
	docker compose up -d

stop:
	@echo "Stopping frontend, backend, and containers..."
	@if lsof -ti :4000,4001,4010,4011 > /dev/null 2>&1; then kill -9 $$(lsof -ti :4000,4001,4010,4011) 2>/dev/null || true; fi
	docker compose down

down: stop

db-seed:
	@bash scripts/seed-db.sh

api:
	@echo "==> Starting PostgreSQL..."
	docker compose up -d
	@echo "==> Waiting for PostgreSQL to be healthy..."
	@until docker compose exec -T postgres pg_isready -U "${DB_USERNAME:-prestamos}" -d "${DB_NAME:-prestamos_facil}" > /dev/null 2>&1; do sleep 2; done
	@echo "PostgreSQL is healthy."
	@if lsof -ti :4010 > /dev/null 2>&1; then echo "Freeing port 4010..."; kill -9 $$(lsof -ti :4010) 2>/dev/null || true; sleep 1; fi
	cd apps/backend && DB_PORT=5432 SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun


backend:
	cd apps/backend && DB_PORT=5432 SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

backend-test:
	cd apps/backend && ./gradlew clean test

backend-check:
	cd apps/backend && ./gradlew clean check

frontend-install:
	cd apps/frontend && pnpm install

frontend:
	cd apps/frontend && pnpm dev

frontend-check:
	cd apps/frontend && pnpm astro check

frontend-test:
	cd apps/frontend && pnpm run --if-present test

frontend-build:
	cd apps/frontend && pnpm build

frontend-lint:
	cd apps/frontend && pnpm lint

frontend-format:
	cd apps/frontend && pnpm format:check

test: backend-check frontend-check frontend-test frontend-lint frontend-format

clean:
	@echo "Cleaning backend..."
	cd apps/backend && ./gradlew clean
	@echo "Cleaning frontend..."
	cd apps/frontend && rm -rf dist .astro node_modules
	@echo "Done."
