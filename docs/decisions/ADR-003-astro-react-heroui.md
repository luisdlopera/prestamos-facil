# ADR-003: Astro + React + HeroUI para el Frontend

## Estado
Aceptado

## Contexto
Se necesita un frontend moderno, rápido, accesible y con buena experiencia de desarrollo. El equipo tiene experiencia en React.

## Decisión
Usar Astro como framework principal con React para componentes interactivos (islands) y HeroUI v3 como librería de componentes UI.

## Razones
- **Astro**: Renderizado estático por defecto, cero JavaScript enviado al cliente sin necesidad. Ideal para contenido y páginas.
- **React**: Ecosistema maduro para componentes interactivos (formularios, tablas). HeroUI requiere React.
- **HeroUI v3**: Componentes accesibles, tema claro/oscuro, basado en React Aria Components, compatible con Tailwind CSS v4.

## Consecuencias
- El HTML inicial se renderiza en el servidor (Astro) y solo se hidrata cuando es necesario
- HeroUI no puede usarse como componente Astro puro; debe ir en archivos `.tsx`
- La configuración de Tailwind CSS v4 es CSS-first (no requiere `tailwind.config.js`)
- No se necesita un provider global de HeroUI

## Alternativas Consideradas
- **Next.js**: SPA pesada para este caso de uso
- **React puro + Vite**: Sin beneficio de islands
- **HeroUI v2**: No compatible con Tailwind v4
