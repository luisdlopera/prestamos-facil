# ADR-001: Monorepo sin Workspaces

## Estado
Aceptado

## Contexto
Se necesita un repositorio único que contenga backend (Java/Spring Boot) y frontend (Astro/React). Las herramientas de build son diferentes (Gradle vs pnpm), y no deben acoplarse.

## Decisión
Usar un monorepo con directorios separados (`apps/backend`, `apps/frontend`). No usar workspaces de Gradle ni npm/pnpm workspaces para unir los builds. Cada aplicación es independiente y se construye por separado.

## Consecuencias
- Builds independientes sin acoplamiento
- Makefile como orquestador común
- Cada equipo puede trabajar en su capa sin conflictos
- No se necesita un build tool que administre ambos

## Alternativas Consideradas
- **Gradle multi-module**: Acopla el frontend al ecosistema Gradle
- **pnpm workspaces**: No gestiona el backend
- **Nx/Turborepo**: Sobrecarga innecesaria para dos proyectos
