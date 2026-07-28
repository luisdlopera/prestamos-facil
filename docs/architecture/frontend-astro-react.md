# Arquitectura del Frontend (Astro + React + HeroUI)

## Estrategia

Astro se utiliza como:
- Router basado en archivos
- Shell de páginas (layout, header, sidebar)
- Renderizado de contenido estático
- Punto de entrada para componentes interactivos React

React se utiliza para:
- Formularios, tablas, modales, filtros (componentes interactivos)
- Componentes HeroUI (son exclusivamente React)
- Lógica del lado del cliente

HeroUI v3 proporciona:
- Componentes accesibles y personalizables
- Tema claro/oscuro
- Diseño responsive

## Reglas

- No usar HeroUI v2 ni NextUI
- No usar componentes HeroUI como componentes Astro puros
- Hibernar componentes React solo cuando sea necesario (`client:load`, `client:visible`, `client:idle`)
- No usar `client:only` de forma generalizada
- Textos visibles en español
- Nombres de archivos, variables, tipos y componentes en inglés

## Estructura

```
src/
├── components/
│   ├── astro/           # Componentes Astro (sin estado)
│   └── react/           # Componentes React (con estado)
├── features/            # Módulos por funcionalidad
│   ├── dashboard/
│   ├── customers/
│   ├── loan-applications/
│   ├── loan-review/
│   ├── payment-plan/
│   └── reports/
├── layouts/
│   └── AppLayout.astro
├── lib/
│   ├── api/             # Cliente HTTP, tipos, errores
│   ├── config/          # Variables de entorno
│   ├── errors/          # Clases de error
│   ├── formatters/      # Formateo de moneda, fechas
│   └── validation/      # Validaciones
├── pages/               # Rutas Astro
├── styles/
│   └── global.css
└── env.d.ts
```

## Cliente HTTP

El cliente HTTP base utiliza `fetch` nativo con:
- `AbortController` para timeouts
- Parseo de `ApiResponse<T>` y `PaginatedApiResponse<T>`
- Manejo de errores canónico (ApiError, NetworkError, TimeoutError)
- Tipado genérico

## HeroUI v3

Los estilos se importan en `global.css`:

```css
@import "tailwindcss";
@import "@heroui/styles";
```
