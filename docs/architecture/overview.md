# Visión General de la Arquitectura

## Diagrama de Contexto

```
[Usuario Navegador]
       |
       | HTTPS
       v
[Frontend Astro + React + HeroUI]  (puerto 4000)
       |
       | HTTP REST + JSON
       v
[Backend Spring Boot]  (puerto 4010)
       |
       | JDBC
       v
[PostgreSQL]  (puerto 5432)
```

## Flujo de Solicitudes

1. El usuario interactúa con componentes React hidratados en páginas Astro.
2. El frontend consume la API REST del backend mediante un cliente HTTP canónico (`ApiResponse<T>`).
3. El backend procesa la solicitud a través de la arquitectura hexagonal:
   - Controladores REST (adaptadores de entrada)
   - Casos de uso de aplicación
   - Entidades de dominio
   - Repositorios (puertos de salida)
4. Los adaptadores de persistencia traducen entre entidades de dominio y entidades JPA.
5. La base de datos PostgreSQL almacena los datos.

## Stack

### Backend
- **Java 21** con Spring Boot 4.1.0
- **Gradle 9.6.1** con Kotlin DSL
- **Arquitectura hexagonal** (puertos y adaptadores)
- **JPA/Hibernate** para persistencia
- **Flyway** para migraciones
- **SpringDoc OpenAPI 3.0.3** para documentación de API
- **Testcontainers** para pruebas de integración
- **ArchUnit** para reglas arquitectónicas
- **JaCoCo** para cobertura de código

### Frontend
- **Astro 7.1.3** como framework de meta-framework
- **React 19.2.8** para componentes interactivos (islands)
- **HeroUI 3.2.2** como librería de componentes UI
- **Tailwind CSS 4.3.3** para estilos
- **TypeScript 5.x** estricto

### Infraestructura
- **PostgreSQL 16** en Docker Compose
- **Docker** para desarrollo local
