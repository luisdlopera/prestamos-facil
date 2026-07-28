# Auditoría Full-Stack — Préstamos Fácil

## Auditoría de seguridad de endpoints — 2026-07-28 (segunda pasada)

Auditoría enfocada en OWASP Top 10 sobre los filtros de autenticación/autorización y la configuración expuesta por la API REST del reto (registro de usuarios, solicitudes de préstamo, aprobación manual/automática, plan de pagos, reportes). Alcance: `LoginRateLimitFilter`, `CookieCsrfFilter`, `JwtTokenConfig`, `SecurityConfig`, `GlobalExceptionHandler`, configuración de Swagger/OpenAPI por perfil.

### Hallazgos críticos corregidos

| # | Categoría OWASP | Hallazgo | Archivo | Fix aplicado |
|---|---|---|---|---|
| 1 | A01 Broken Access Control | El rate limit de login/registro/refresh confiaba ciegamente en el header `X-Forwarded-For` enviado por el cliente. Un atacante que se conecta directamente (sin pasar por un proxy real) podía enviar un valor distinto de `X-Forwarded-For` en cada intento y evadir el límite de 20 intentos/15min, habilitando fuerza bruta sobre credenciales. | `LoginRateLimitFilter.java` | El header solo se honra si `remoteAddr` (IP real del socket) pertenece a una lista de proxies de confianza configurable (`app.security.trusted-proxies`). Sin esa lista configurada, siempre se usa la IP real de la conexión TCP. |
| 2 | A04 Insecure Design | La validación CSRF sobre cookies (`/auth/refresh`, `/auth/logout`, `/staff/refresh`, `/staff/logout`) aceptaba un `Referer` que hiciera `startsWith(origenPermitido + "/")`. Un referer como `http://attacker.com/http://localhost:4000/` supera ese `startsWith` en ciertos parsers y podía colar peticiones cross-site contra los endpoints basados en cookie. | `CookieCsrfFilter.java` | El `Origin`/`Referer` ahora se parsean con `java.net.URI` y se comparan por `scheme` + `host` + `port` exactos contra el origen permitido, no por coincidencia de prefijo de string. |
| 3 | A02 Cryptographic Failures | `application.yml` define un secreto JWT por defecto (`dev-secret-replace-in-production-at-least-32-chars`) que decodifica a 37 bytes válidos para HS256. Si se despliega en producción sin definir la variable de entorno `JWT_SECRET`, la aplicación arranca igual usando ese secreto público conocido en el repositorio, permitiendo forjar tokens de acceso/staff arbitrarios. | `JwtTokenConfig.java` | El arranque ahora falla (`IllegalStateException`) cuando el perfil activo es `prod` y la variable de entorno `JWT_SECRET` no está definida explícitamente, sin importar el valor por defecto de la propiedad. |

### Hallazgos de configuración corregidos

| # | Hallazgo | Fix aplicado |
|---|---|---|
| 4 | Swagger UI (`/swagger-ui/**`) y OpenAPI (`/v3/api-docs/**`) quedaban públicamente accesibles también en producción, exponiendo el mapa completo de la API a cualquier visitante no autenticado. | Se agregó `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false` en `application-prod.yml`, desactivando los endpoints por completo bajo ese perfil. |

### Hallazgos evaluados y no modificados (riesgo bajo, decisión consciente)

- **`GlobalExceptionHandler`** expone `field` y `code` en errores 400 de validación. Se evaluó genérica-lo, pero es una práctica estándar de UX de API REST (el frontend depende de esos campos para resaltar inputs); no hay fuga de datos sensibles, solo nombres de campos del propio DTO público. Se deja sin cambios.
- **Límite de sesiones concurrentes de staff** y **TTL explícito de password reset** quedan documentados como mejoras recomendadas (ver sección de recomendaciones), no bloquean el reto ni representan una vulnerabilidad explotable de forma directa hoy.

### Pruebas añadidas/actualizadas

- `LoginRateLimitFilterTest` (nuevo): reproduce el intento de bypass con `X-Forwarded-For` spoofeado desde un origen no confiable (debe seguir bloqueando por IP real) y el caso correcto de un proxy de confianza que sí debe poder diferenciar clientes reales detrás de él.
- `CookieCsrfFilterTest` (nuevo): reproduce el referer ambiguo (`http://attacker.com/http://localhost:4000/...`) y confirma que ahora es rechazado con 403; valida los casos legítimos de `Origin`/`Referer` y el rechazo por puerto distinto.
- `JwtTokenConfigTest`: se agregó caso que verifica que el arranque falla en perfil `prod` sin `JWT_SECRET` explícito; se actualizó la firma de construcción (ahora recibe `Environment`) en este archivo y en `TokenGenerationServiceTest`/`TokenValidationServiceTest`.
- Verificación: `./gradlew compileJava` y `./gradlew test --tests "...auth.filter.*" --tests "...security.*"` — compilación limpia y suite en verde.

### Recomendaciones pendientes (no bloqueantes)

1. Limitar sesiones concurrentes de staff o asociar refresh tokens a dispositivo/fingerprint.
2. Confirmar y, si falta, imponer un TTL corto (≤1h) explícito sobre el token de recuperación de contraseña en `PasswordResetUseCase`.
3. Añadir logging de intentos de autenticación fallidos (actualmente el filtro no deja rastro estructurado más allá del contador en caché) para dar soporte a detección de fuerza bruta a nivel de observabilidad.
4. Considerar pruebas de integración con `@SpringBootTest` + Testcontainers que carguen la cadena de filtros de Spring Security real y verifiquen escalación de privilegios extremo a extremo (cliente intentando aprobar/rechazar préstamos, acceder a datos de otro cliente, etc.); las pruebas actuales de este ciclo cubren los filtros en aislamiento.

---

> Actualización de seguridad aplicada el 2026-07-28. Este apartado supersede cualquier estado histórico que contradiga el código actual.

## Correcciones de autenticación y autorización — 2026-07-28

- Se reemplazó la autorización sensible basada únicamente en `ROLE_STAFF` por authorities explícitas: `CUSTOMER_CREATE`, `LOAN_APPLICATION_*`, `LOAN_READ_*`, `PAYMENT_PLAN_READ_*` y `REPORT_APPROVED_LOANS_READ`.
- Se habilitó `@EnableMethodSecurity` y se agregaron comprobaciones equivalentes en controladores y casos de uso.
- Las consultas de detalle de solicitudes y préstamos incluyen `customerId`; la verificación de propiedad ocurre también en la capa de aplicación.
- Se preservó el control de alcance en listados: un cliente nunca puede convertir `customerId`, `search`, estado u ordenamiento en acceso fuera de su conjunto autorizado.
- El registro de staff acepta únicamente `ANALYST`, `CREDIT_ANALYST`, `SUPERVISOR` o `AUDITOR`; `ADMIN` no puede autoconcederse mediante el DTO.
- Refresh valida el tipo, expiración, hash persistido, identidad del `sub` y revocación. El registro JPA se bloquea durante el consumo para impedir doble rotación concurrente.
- Rate limiting cubre login, registro y refresh por combinación de endpoint e IP.

## Incidencia reproducida y corregida — clientes y recuperación

- El `403` de clientes no era una falta de permiso del rol `CUSTOMER`: `JwtAuthFilter` ejecutaba `findByUserId`, cuyo `CustomerEntity.user` era lazy. El mapper accedía al correo después de cerrar la sesión JPA y lanzaba `LazyInitializationException`; el filtro no podía construir la autenticación.
- `findByUserId` ahora usa `JOIN FETCH` y los listados de solicitudes se ejecutan dentro de una transacción de solo lectura, evitando el mismo fallo durante el mapeo batch.
- La interfaz de recuperación del frontend aceptaba `*` como carácter especial, pero `PasswordPolicy` del backend no. Se alineó la validación para que ambos lados acepten exactamente `!@#$%^&+=`.
- Se agregaron pruebas de integración para el lookup usado por JWT, el flujo de registro/login/refresh/password reset y pruebas del catálogo de permisos.

## Registro sin sesión — contrato vigente

`POST /auth/register` es deliberadamente un endpoint de alta, no de autenticación. Devuelve el perfil creado, pero no genera access token ni refresh cookie. El frontend limpia cualquier sesión previa y redirige a `/login?registered=1`; únicamente un login exitoso establece la sesión.

Verificación: `./gradlew clean check` — 277 pruebas ejecutadas, 269 correctas y 8 de integración omitidas por ausencia de Docker/Testcontainers. Frontend: 26 pruebas correctas.

**Fecha**: 2026-07-26  
**Auditor**: Sistema de auditoría automatizada  
**Versión del análisis**: 1.0  
**Commit analizado**: Working tree actual

---

## A. Resumen Ejecutivo

Se auditaron 56+ archivos backend (Java/Spring Boot 4.1.0), 70+ archivos frontend (Astro 7/React 19), base de datos PostgreSQL 16, configuración Docker, y servicios activos. Se encontraron **4 problemas críticos**, **12 de alta severidad**, **5 de media**, y **3 de baja**.

**Hallazgo principal**: Ningún login demo funciona — los bcrypt hashes seed no coinciden con la contraseña documentada `!Pass.1234`. El backend compila pero sus tests unitarios no. El frontend construye sin errores y sus tests pasan. El sistema de email funciona correctamente vía Mailpit.

## Revisión de la auditoría — 2026-07-26

Se contrastó este informe contra el código actual, las pruebas y los servicios locales. El diagnóstico original era parcialmente correcto, pero mezclaba problemas reales con afirmaciones ya resueltas o no reproducibles en el código vigente.

### Estado después de la verificación y correcciones

| Hallazgo | Veredicto | Evidencia actual |
|---|---|---|
| C1 — credenciales demo | **Confirmado en la DB local; corregido** | La DB tenía hashes divergentes. Se sincronizaron las tres cuentas y ambas rutas de login respondieron HTTP 200. Se agregó la migración V27 para futuras ejecuciones. |
| C2 — login inválido devuelve 400 | **Confirmado; corregido** | Las credenciales inválidas ahora usan una excepción de autenticación y responden HTTP 401. |
| C3/A9 — logout no envía cookie refresh | **Confirmado; corregido** | `/auth/logout` y `/staff/logout` fueron incluidos en las rutas que usan `credentials: "include"`. |
| C4 — test backend no compila | **Confirmado; corregido** | Se actualizó el fixture con `UserRepository`; `./gradlew clean test` pasa. |
| A3 — reset solo busca customers | **Confirmado; corregido** | La solicitud y confirmación ahora soportan también usuarios staff. |
| A5/A6 — ADMIN/ANALYST sin acceso STAFF | **No confirmado en el código actual** | `JwtAuthFilter` ya asigna `ROLE_STAFF` a todo token staff y `SecurityConfig` usa ese rol. |
| A1 — timestamps de CustomerResponse | **Contrato desalineado** | El backend no expone timestamps en `CustomerResponse`; el frontend dejó de declararlos y de ordenar por un campo inexistente. |
| A2 — falta `loanTypeId` | **Confirmado; corregido** | Se añadió `loanTypeId` al DTO backend y su mapeo. |
| M3–M5/B1 — errores TypeScript/ESLint | **Confirmados; corregidos** | Se eliminaron imports sin uso y se preservaron las causas de errores. |
| A10 — SameSite=Lax | **No es un fallo concluyente** | La aplicación tiene filtro CSRF por Origin/Referer y el refresh es POST. No se cambió sin conocer el dominio final del despliegue. |

### Verificaciones ejecutadas

- Backend: `./gradlew clean test` — **BUILD SUCCESSFUL**.
- Frontend: TypeScript, Vitest (**10 suites / 23 tests**), ESLint y Astro build — **todo correcto**; build de 17 páginas.
- Runtime: `/actuator/health` — **HTTP 200 / UP**.
- Login real: admin por `/api/v1/staff/login` y cliente por `/api/v1/auth/login` — **HTTP 200** con `!Pass.1234`.

Los conteos globales de severidad y los resultados históricos de Mailpit no se consideran reproducidos por sí solos; dependen del estado de los servicios en el momento de la auditoría.

---

## B. Estado General del Sistema

| Componente | Estado | Evidencia |
|---|---|---|
| **Frontend (puerto 4000)** | ✅ Build OK, tests OK | `pnpm run build` → 17 páginas, 572ms; `pnpm test` → 10 suites, 23 tests passed |
| **Backend (puerto 4010)** | ⚠️ Compila, tests no pasan | `./gradlew clean test` → FAIL (error de compilación en test) |
| **PostgreSQL (puerto 5432)** | ✅ Running en Docker | `pg_isready` ok, healthcheck ok, 8 usuarios seed |
| **Mailpit (puertos 4025/4050)** | ✅ Running | `docker ps` ok, 4 emails capturados |
| **Actuator health** | ✅ UP | `curl /actuator/health` → `{"status":"UP"}` |
| **Login demo** | ❌ **Todos fallan** | HTTP 400 Bad Request, mensaje "Credenciales inválidas" |
| **Loan Types API** | ✅ Funciona | `GET /api/v1/loan-types` → 5 tipos de préstamo retornados |
| **Password Reset** | ✅ Envío de emails funcional | Mailpit muestra 4 correos con tokens de restablecimiento |
| **Tests Backend** | ❌ No compilan | `PasswordResetUseCaseImplTest` falta parámetro `UserRepository` |
| **Tests Frontend** | ✅ 10/10 passed | 23 tests, 712ms de ejecución |
| **TypeScript** | ⚠️ 2 errores | `Input` y `TrendingUp` importados pero no usados |
| **ESLint** | ⚠️ 6 errores | 2 unused imports + 4 errores `preserve-caught-error` |
| **Swagger UI** | ✅ Disponible | `/swagger-ui.html` configurado con OpenAPI |

---

## C. Tabla de Problemas Priorizados

### Críticos

| ID | Archivo | Línea | Problema | Impacto | Reproducción |
|---|---|---|---|---|---|
| **C1** | Migrations V13, V15, V25, V26 | Varias | **Ningún bcrypt hash seed coincide con `!Pass.1234`**. En DB: admin tiene `$2a$10$wu/...` (distinto a seed `$2b$12$...` de V26). Cliente tiene `$2b$10$...` que tampoco coincide. | Usuarios demo no pueden iniciar sesión. Sistema inutilizable para nuevos desarrolladores. README documenta credenciales que no funcionan. | `curl -X POST http://localhost:4010/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@prestamosfacil.com","password":"!Pass.1234"}'` → 400 "Credenciales inválidas" |
| **C2** | `GlobalExceptionHandler.java` | 74-78 | `ApplicationException` (incluyendo "Credenciales inválidas") retorna HTTP **400** en vez de **401**. El frontend no distingue error de autenticación vs error de validación. | El frontend no puede diferenciar un login inválido de un error de datos. Ataques de fuerza bruta no dejan traza clara. | Hacer login con credenciales incorrectas → HTTP 400 en vez de 401. |
| **C3** | `client.ts` (frontend) | 8 | `/auth/logout` y `/staff/logout` no están en `REFRESH_PATHS`. La petición de logout no incluye `credentials: "include"`. La cookie httpOnly refresh **nunca se envía** al servidor. | Cierre de sesión no revoca el refresh token en el servidor. Token sigue válido 7 días. Sesión podría ser reutilizada. | Hacer logout → verificar con `docker exec prestamos-facil-db psql -U prestamos -d prestamos_facil -c "SELECT * FROM auth_tokens WHERE revoked=false"` → token sigue activo. |
| **C4** | `PasswordResetUseCaseImplTest.java` | 49 | Test no compila: constructor de `PasswordResetUseCaseImpl` ahora requiere 7 parámetros (se agregó `UserRepository`) pero el test pasa 6. | `./gradlew test` falla. No se pueden ejecutar pruebas del backend. | `./gradlew clean test` → `error: constructor PasswordResetUseCaseImpl cannot be applied to given types` |

### Alta

| ID | Archivo | Línea | Problema | Impacto |
|---|---|---|---|---|
| **A1** | `CustomerResponse.java` vs `customers-api.ts` | varios | Backend tiene `phoneCountryCode`/`phoneNumber`. Frontend espera `createdAt`/`updatedAt` inexistentes. | Al frontend le faltan timestamps de auditoría. El backend envía campos que el frontend ignora. |
| **A2** | `LoanApplicationResponse.java` vs `loan-applications-api.ts` | varios | Frontend `LoanApplicationDto` espera `loanTypeId`, el backend no lo incluye. | No se puede enlazar la solicitud con el tipo de préstamo desde la respuesta. |
| **A3** | `PasswordResetUseCaseImpl.java` | 54 | `requestPasswordReset()` solo busca en `customerRepository`, **nunca en `userRepository`**. | Admin y analista no pueden solicitar restablecimiento de contraseña. |
| **A4** | `UserAuthenticationUseCaseImpl.java` | 109-110 | `login()` busca solo por email en users. Si email no existe en `users` (caso legacy), login falla. | Dependencia de consistencia entre tablas `users` y `customers`. |
| **A5** | `SecurityConfig.java` | 81 | `API_STAFF + "/**"` requiere `hasRole("STAFF")`. ADMIN tiene `ROLE_ADMIN`, no `ROLE_STAFF`. | ADMIN no puede acceder a endpoints staff (`/staff/me`, etc.). |
| **A6** | `JwtAuthFilter.java` | 80-87 | Para ADMIN: agrega `ROLE_STAFF` y `ROLE_ADMIN`. Analista: solo `ROLE_ANALYST` (sin `ROLE_STAFF`). | Analista no puede acceder a rutas staff. |
| **A7** | `LoginRateLimitFilter.java` | varios | 20 intentos/15 min por IP. Sin endpoint de desbloqueo. | Bloqueo permanente de login hasta reinicio de app. |
| **A8** | `EmailService.java` | varios | Token de reset visible en URL. Almacenado en Mailpit (logs). | Exposición del token a través de logs de email. |
| **A9** | Frontend `client.ts` + Backend `logoutByToken` | varios | Logout sin credentials → cookie no enviada → token no revocado. | Sesión no se cierra realmente en backend. |
| **A10** | `AuthCookieFactory.java` | 48 | `SameSite=Lax` configurado (no `Strict`). | Ver sección "SameSite=Strict: ¿Por qué es necesario?" más abajo. |

### Media

| ID | Archivo | Línea | Problema |
|---|---|---|---|
| **M1** | `application.yml` | 7 | Default profile `email` no existe como archivo. `application-local.yml` con config Flyway importante nunca se activa. |
| **M2** | `db/dev-seed/demo-data.sql` | todas | Script inserta en tablas `staff`/`staff_roles`/`customers` obsoletas (V14 las unificó). Ejecutarlo dañaría la DB. |
| **M3** | `LoginPage.tsx` | 4 | Importa `Input` pero nunca lo usa (usa `InputGroup.Input`). |
| **M4** | `PasswordInput.tsx` | 2 | `Input` importado pero no usado. Error TS. |
| **M5** | `DashboardChart.tsx` | 5 | `TrendingUp` importado pero no usado. Error TS. |

### Baja

| ID | Archivo | Línea | Problema |
|---|---|---|---|
| **B1** | `auth-service.ts` | 115,118,121,123 | Errores lanzados sin `cause`. Dificulta debugging. |
| **B2** | `auth.store.ts` | varios | Access Token JWT persistido en localStorage. Vulnerable a XSS. |
| **B3** | Varios | varios | `UserType` enum tiene valores `CUSTOMER/STAFF/ADMIN`, código a veces usa strings. |

---

## D. Flujo de Autenticación Auditado

```
LOGIN FLOW (frontend → backend):
  1. LoginPage.tsx → loginUnified(email, password)
  2. auth-service.ts loginUnified():
     a. POST /api/v1/auth/login → 400 (bcrypt hash inválido)
     b. Fallback: POST /api/v1/staff/login → 400
     c. Lanza "Credenciales inválidas"
  3. Resultado: Nunca se puede iniciar sesión con credenciales demo

REFRESH FLOW:
  1. client.ts attemptTokenRefresh():
     a. POST /api/v1/auth/refresh (credentials: "include")
     b. Extrae accessToken de response.data.accessToken
     c. Si falla: POST /api/v1/staff/refresh (credentials: "include")
  2. Backend: AuthCookieFactory.extractRefreshToken() lee cookie "prestamos_refresh"
  3. Backend: refresh() → valida JWT refresh → revoca old → emite new pair
  4. Flujo correcto (cuando funciona)

LOGOUT FLOW (ROTO):
  1. auth-service.ts logout():
     a. POST /auth/logout (SIN credentials: "include") ← C3
     b. Backend: extractRefreshToken() → null → logoutByToken(null) → no-op
     c. clearRefreshCookie() envía Set-Cookie con maxAge=0
  2. Browser acepta Set-Cookie pero token en DB no se revoca
  3. Token sigue válido hasta expiración (7 días)

SESSION PERSISTENCE:
  1. AuthProvider.tsx: mount → fetchProfile() → GET /auth/me (Bearer token)
  2. Si token expira: attemptTokenRefresh() → refresh → nuevo token
  3. Zustand persist guarda accessToken en localStorage (B2)
  4. Al recargar: Zustand restaura estado → fetchProfile() valida

PASSWORD RESET (PARCIALMENTE ROTO):
  1. ForgotPasswordPage: POST /auth/password-reset/request { email }
  2. Backend: busca en customerRepository → genera token → envía email
  3. Email via Mailpit: template Thymeleaf con enlace
  4. Usuario hace clic → POST /auth/password-reset/confirm { token, newPassword }
  5. Backend: valida token → cambia password → revoca refresh tokens
  6. BUG (A3): admin/analista NUNCA reciben email (solo busca en customers)
```

---

## E. SameSite=Strict: ¿Por qué es necesario?

### Contexto

La cookie `prestamos_refresh` (httpOnly) tiene configurado `SameSite=Lax`.

```yaml
# application.yml
app.security.cookie.same-site: Lax
```

### ¿Qué significa SameSite?

- **`Lax`** (default moderno): La cookie se envía en navegaciones de nivel superior (top-level navigations) desde otros sitios, pero NO en subrequests cross-site (fetch, XHR, imágenes, iframes). Sí se envía cuando el usuario hace clic en un enlace hacia tu sitio.

- **`Strict`**: La cookie NUNCA se envía en ningún tipo de solicitud cross-site, ni siquiera navegaciones de nivel superior.

### Escenario de ataque con Lax

1. Un atacante envía un enlace por email: `<a href="https://prestamosfacil.com/loan-applications">Ver oferta</a>`
2. Si el usuario tiene sesión activa y hace clic, el navegador envía SOLO la cookie refresh (SameSite=Lax se envía en navegaciones GET de nivel superior)
3. La página carga con la petición GET. Aunque el ataque CSRF vía GET es limitado, la cookie `prestamos_refresh` queda expuesta

### ¿Por qué Strict es mejor para cookies de autenticación?

1. **La cookie refresh es el "master key"** — permite obtener nuevos access tokens. Debería tener la protección más estricta disponible.

2. **El refresh endpoint es POST, no GET** — SameSite=Lax ya protege POST cross-site, pero Strict es redundancia defensiva.

3. **El CookieCsrfFilter ya existe** — el filtro verifica Origin/Referer, pero Strict opera a nivel de navegador. Dos capas de defensa.

### Consideración: ¿Strict rompe algo?

- **Password reset** — Los enlaces de reset password son a `GET /auth/forgot-password?token=...`. Estos normalmente vienen de un email (cross-site). Con Strict, la cookie NO se enviaría al cargar la página. **Sin embargo, la página de reset password no necesita autenticación**, por lo que no necesita cookies. Strict es seguro aquí.

- **Navegación desde bookmark** — Tampoco sería problema porque cookies de refresh no se necesitan para GET requests a páginas públicas.

### Veredicto

**SameSite=Strict es la opción correcta para cookies de autenticación (access y refresh)**. SameSite=Lax es aceptable con el CookieCsrfFilter existente, pero Strict proporciona:
- Protección máxima contra CSRF a nivel de navegador
- Defensa en profundidad (independiente del filtro Origin/Referer)
- Alineación con OWASP ASVS (V3.2.1)

No hay impacto funcional porque:
1. El refresh endpoint es POST y siempre same-site o requiere Origin válido
2. Las páginas públicas no necesitan la cookie
3. Las páginas protegidas cargan con access token (Bearer header), no con cookie

---

## F. Problemas de Integración Frontend/Backend

| Endpoint Frontend | Backend Contract | ¿Coinciden? |
|---|---|---|
| `POST /auth/login` | `LoginRequest(email, password)` → `LoginResponse \| StaffLoginResponse` | ✅ Formato correcto |
| `POST /staff/login` | `StaffLoginRequest(email, password)` → `StaffLoginResponse` | ✅ |
| `GET /auth/me` | → `AuthUserResponse` | ✅ |
| `GET /customers?page&size&search` | → `PaginatedResponse<CustomerResponse>` | ⚠️ `CustomerResponse` no tiene `createdAt`/`updatedAt` (A1) |
| `POST /customers` | `CreateCustomerRequest` → `CustomerResponse` | ✅ |
| `GET /loan-applications` | → `PaginatedResponse<LoanApplicationResponse>` | ⚠️ Falta `loanTypeId` (A2) |
| `POST /loan-applications/{id}/approve` | → `LoanApplicationResponse` | ✅ |
| `POST /loan-applications/{id}/reject` | `RejectRequest(reason)` → `LoanApplicationResponse` | ✅ |
| `POST /loan-applications/{id}/automatic-evaluation` | → `AutomaticEvaluationResponse` | ✅ (extra fields ignorados) |
| `GET /loans?page&size&search&customerId` | → `PaginatedResponse<LoanResponse>` | ✅ |
| `GET /loans/{id}/payment-plan` | → `ApiResponse<List<PaymentInstallmentResponse>>` | ✅ |
| `GET /reports/approved-loans/total` | → `ApiResponse<ApprovedLoansTotalResponse>` | ✅ |
| `GET /loan-types` | → `ApiResponse<List<LoanTypeResponse>>` | ✅ |
| `GET /loan-types/admin` | → `ApiResponse<List<LoanTypeResponse>>` + pagination | ⚠️ Frontend trata como `ApiResponse` no `PaginatedResponse` |
| `POST /loan-types` | `CreateLoanTypeRequest` → `LoanTypeResponse` | ✅ |
| `PUT /loan-types/{id}` | `UpdateLoanTypeRequest` → `LoanTypeResponse` | ✅ |
| `PATCH /loan-types/{id}/status` | `ToggleLoanTypeStatusRequest(active)` → `LoanTypeResponse` | ✅ |
| `POST /loan-types/reorder` | `ReorderLoanTypesRequest(orderedIds)` → vacío | ✅ |
| `DELETE /loan-types/{id}` | → vacío (204) | ✅ |

---

## G. Problemas de Seguridad

| Problema | Severidad | Detalle | Mitigación |
|---|---|---|---|
| Login retorna 400 en vez de 401 | Alta | Ataques de fuerza bruta no distinguibles de errores de validación | Retornar 401 |
| Cookie refresh sin SameSite=Strict | Media | Exposición a ataques CSRF en nivel de navegador | Cambiar a Strict |
| Token reset en URL de email | Media | Visible en logs de Mailpit y barra de direcciones | Usar método POST o hash |
| Access Token en localStorage | Media | Vulnerable a XSS | Migrar a cookie httpOnly |
| Rate limit sin desbloqueo | Media | Bloqueo permanente | Agregar endpoint de desbloqueo o decay exponencial |
| ADMIN sin acceso a rutas STAFF | Alta | Admin no tiene ROLE_STAFF | Usar hasAnyRole("ADMIN", "STAFF") |
| ANALYST sin ROLE_STAFF | Alta | Analista no tiene ROLE_STAFF | Agregar ROLE_STAFF a todos los staff |
| Logout no revoca token | Alta | Token queda activo 7 días | Agregar credentials: include al logout |

---

## H. Problemas de Base de Datos

| Problema | Severidad | Detalle |
|---|---|---|
| Bcrypt hash seed inválido | **Crítica** | Ningún hash coincide con `!Pass.1234`. Admin tiene hash `$2a$10$wu/...` (distinto a seed). Todos los logins demo fallan. |
| demo-data.sql obsoleto | **Alta** | Script inserta en tablas `staff`/`customers` que ya no existen (V14-V20). Destructivo si se ejecuta. |
| Mezcla `$2a$` y `$2b$` en hashes | Media | Spring Security usa `$2a$`, seed usa `$2b$`. Compatible pero inconsistente. |
| Flyway out-of-order: false | Media | 26 migrations con dependencias entre versiones. Si se corre en orden incorrecto, falla. |
| updated_at sin trigger | Baja | Algunas tablas no actualizan `updated_at` automáticamente. |

### Estado actual de los hashes en DB

```sql
SELECT email, password_hash, role FROM users WHERE email LIKE '%@prestamosfacil.com';

            email            |                        password_hash                         |   role   
-----------------------------+--------------------------------------------------------------+----------
 analista@prestamosfacil.com | $2b$12$aXAYb8C92XPyBmsC3SEBaO0OaAPNAQydpRICdktQhZkfAxAJPmF3. | ANALYST
 admin@prestamosfacil.com    | $2a$10$wu/xa5frWUOMyxQOB3i14e419OO3KWKWusjN0XgxIbtJt6JFGw24S | ADMIN
 cliente@prestamosfacil.com  | $2b$10$s/5GiP8dUPJI8gZJFzJOruqhGBVWG3lqUbMH7JWK3Oqkzfz0vxKqS | CUSTOMER
```

Ninguno de estos hashes corresponde a `!Pass.1234`. El hash de admin (`$2a$10$wu/...`) es diferente a cualquier seed en las migrations — alguien o algo lo sobreescribió después de V26.

---

## I. Problemas de Email

| Problema | Severidad | Detalle |
|---|---|---|
| Mailpit funcionando | ✅ | Puerto SMTP 4025, UI 4050, 4 mensajes capturados |
| Reset password solo para customers (A3) | **Alta** | `requestPasswordReset()` busca solo en `customerRepository`. Admin/analista excluidos. |
| Token en URL (A8) | Media | `frontendUrl + "/auth/forgot-password?token=" + token` — visible en logs y navegador |
| Notificaciones no implementadas | Media | `EmailNotificationAdapter` + `OutboxEvent` existen pero eventos no están programados. No se notifican solicitudes/aprobaciones/rechazos. |

---

## J. Resultados de Pruebas Ejecutadas

| Comando | Resultado | Detalle |
|---|---|---|
| `pnpm exec tsc --noEmit` | ❌ 2 errors | Unused imports: `Input`, `TrendingUp` |
| `pnpm test` (vitest) | ✅ 10/10 passed | 23 tests, 712ms |
| `pnpm run lint` | ❌ 6 errors | 2 unused imports + 4 `preserve-caught-error` |
| `pnpm run build` | ✅ Build exitoso | 17 páginas en 1.24s |
| `./gradlew clean test` | ❌ FAIL | `PasswordResetUseCaseImplTest` no compila (C4) |
| `curl /actuator/health` | ✅ UP | |
| `POST /auth/login` admin | ❌ 400 | "Credenciales inválidas" (C1, C2) |
| `POST /auth/login` cliente | ❌ 400 | "Credenciales inválidas" (C1, C2) |
| `POST /staff/login` admin | ❌ 400 | "Credenciales inválidas" (C1, C2) |
| `POST /auth/password-reset/request` | ✅ 200 | Email enviado a Mailpit |
| `GET /api/v1/loan-types` | ✅ 200 | 5 tipos de préstamo |

---

## K. Plan de Corrección por Prioridad

### Fase 1: Correcciones Críticas (inmediato, < 1 día)

| # | Acción | Archivos |
|---|---|---|
| 1 | Generar bcrypt hash correcto de `!Pass.1234` y crear migration V27 que actualice `password_hash` para admin, analista y cliente | `db/migration/V27__fix_all_demo_passwords.sql` |
| 2 | Login inválido debe retornar HTTP 401, no 400. Usar `ApiException` con HttpStatus `UNAUTHORIZED` en login | `UserAuthenticationUseCaseImpl.java`, `GlobalExceptionHandler.java` |
| 3 | Agregar `/auth/logout` y `/staff/logout` a `REFRESH_PATHS` en el cliente | `frontend/src/lib/api/client.ts:8` |
| 4 | Agregar `UserRepository` mock al test constructor | `PasswordResetUseCaseImplTest.java:49` |

### Fase 2: Seguridad y Roles (1-3 días)

| # | Acción | Archivos |
|---|---|---|
| 5 | Cambiar `hasRole("STAFF")` a `hasAnyRole("STAFF", "ADMIN")` en SecurityConfig | `SecurityConfig.java:70-81` |
| 6 | En `JwtAuthFilter`, agregar `ROLE_STAFF` a todos los usuarios de tipo staff (no solo ADMIN) | `JwtAuthFilter.java:80-87` |
| 7 | Ampliar `requestPasswordReset()` para buscar también en `userRepository` | `PasswordResetUseCaseImpl.java:54` |
| 8 | Cambiar `SameSite=Lax` a `SameSite=Strict` en cookies | `application.yml:79` |
| 9 | Agregar `createdAt`/`updatedAt` a `CustomerResponse` y `loanTypeId` a `LoanApplicationResponse` | DTOs backend |
| 10 | Agregar `cause` a errores en auth-service.ts | `frontend/src/features/auth/infrastructure/auth-service.ts` |

### Fase 3: Calidad y Mantenimiento (3-5 días)

| # | Acción |
|---|---|
| 11 | Cambiar default profile de `email` a `local` o crear `application-email.yml` |
| 12 | Eliminar/actualizar `db/dev-seed/demo-data.sql` obsoleto |
| 13 | Limpiar imports no usados: `Input` en `PasswordInput.tsx` y `LoginPage.tsx`, `TrendingUp` en `DashboardChart.tsx` |
| 14 | Implementar notificaciones por email vía outbox pattern |

### Fase 4: Mejoras (futuro)

| # | Acción | Motivo |
|---|---|---|
| 15 | Migrar access token de localStorage a cookie httpOnly | Mitigación XSS |
| 16 | Agregar endpoint de desbloqueo de rate limit | UX para usuarios bloqueados |
| 17 | Agregar prueba E2E del flujo completo | Prevenir regresiones |
| 18 | Agregar contract testing automático entre frontend y backend DTOs | Detectar discrepancias temprano |

---

## L. Lista de Pruebas Faltantes

### Pruebas de Integración (deben agregarse después de Fase 1)

| Prueba | Descripción |
|---|---|
| Login con credenciales correctas | Verificar que login retorna 200, accessToken, refresh cookie |
| Login con credenciales incorrectas | Verificar que retorna 401 (no 400) |
| Refresh token con cookie httpOnly | POST /auth/refresh sin cookie → 401; con cookie → 200 + nuevo token |
| Logout real | POST /auth/logout → verificar revocación en auth_tokens |
| Password reset completo | Request → confirmar en Mailpit → confirm reset → login con nueva password |
| Roles y permisos | ADMIN accede a `/staff/me` ✅; CUSTOMER accede a `/staff/me` → 403 |
| Aprobación de solicitud | CUSTOMER intenta approve → 403; STAFF → 200 |

### Pruebas de Seguridad

| Prueba | Descripción |
|---|---|
| Rate limit | 21 intentos en 1 min → HTTP 429 |
| CSRF en cookies | POST /auth/refresh sin Origin → 403 |
| SameSite | Navegación cross-site desde enlace externo → cookie no enviada |
| IDOR | CUSTOMER-A intenta ver préstamo de CUSTOMER-B → 404/403 |
| XSS | Access token en localStorage no debería ser accesible por scripts inline |

### Pruebas de DB

| Prueba | Descripción |
|---|---|
| Hash bcrypt válido | `BCryptPasswordEncoder().matches("!Pass.1234", hash)` debe ser `true` |
| Migración V27 rollback | `flyway.undo` o migration de rollback |
| Integridad referencial | No hay customer sin user, no hay loan_application sin customer |

### Pruebas Contract (DTOs)

| Prueba | Descripción |
|---|---|
| CustomerResponse vs CustomerDto | Coincidencia de campos (nombre, tipo) |
| LoanApplicationResponse vs LoanApplicationDto | Coincidencia de campos |
| StaffLoginResponse vs tipo esperado | `id` vs `user.id`, `name` vs `firstName/lastName` |

---

## Apéndice: Servicios Activos

| Puerto | Proceso | PID | Tecnología |
|---|---|---|---|
| 4000 | node | 19982 | Astro 7 / React 19 (Frontend) |
| 4010 | java | 15138 | Spring Boot 4.1.0 / Java 21 (Backend) |
| 5432 | com.docke (Docker) | 82677 | PostgreSQL 16 (contenedor `prestamos-facil-db`) |
| 4025 | axllent/mailpit | Docker | Mailpit SMTP |
| 4050 | axllent/mailpit | Docker | Mailpit UI |

### Variables de Entorno

```
PUBLIC_API_BASE_URL=http://localhost:4010/api/v1
FRONTEND_ORIGIN=http://localhost:4000
HTTP_PORT=4010
DB_HOST=localhost
DB_PORT=5432
DB_NAME=prestamos_facil
DB_USERNAME=prestamos
DB_PASSWORD=prestamos_local
MAIL_HOST=localhost
MAIL_PORT=4025
MAILPIT_SMTP_PORT=4025
MAILPIT_UI_PORT=4050
JWT_SECRET=dev-secret-replace-in-production-at-least-32-chars
```
