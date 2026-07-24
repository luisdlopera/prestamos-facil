# Préstamos Fácil

Sistema de gestión de solicitudes de préstamo construido como una API REST con Java, Spring Boot, PostgreSQL y arquitectura hexagonal.

## Cumplimiento de los 6 requerimientos funcionales

| # | Requerimiento | Estado |
|---:|---|---|
| 1 | Registrar usuarios con datos obligatorios, correo válido y único, y salario entre `0` y `15.000.000`. | **Cumplido** |
| 2 | Registrar solicitudes validando cliente, tipo de préstamo y estado inicial `PENDING_REVIEW`. | **Cumplido** |
| 3 | Consultar solicitudes con filtro por estado, paginación y datos completos del solicitante y del préstamo. | **Cumplido** |
| 4 | Aprobar o rechazar manualmente; al aprobar, calcular cuota, generar plan de pagos y notificar. | **Cumplido** |
| 5 | Evaluar automáticamente mediante Stored Procedure aplicando capacidad del 35%, deuda actual, cuota nueva y regla de cinco salarios. | **Cumplido** |
| 6 | Consultar el reporte consolidado del valor total de préstamos aprobados. | **Cumplido** |

## Requisitos del reto

| Requisito | Implementación |
|---|---|
| Java 17 o superior | Java 21 mediante Gradle Toolchain. |
| Spring Boot 3 o superior | Spring Boot 4.1.0. |
| Arquitectura hexagonal | `domain` contiene reglas y puertos; `application` contiene casos de uso; `infrastructure` contiene adaptadores REST, JPA, seguridad, Flyway, Stored Procedure y notificaciones. |
| Pruebas | JUnit, Mockito, Spring Boot Test, MockMvc, ArchUnit y una prueba de integración PostgreSQL con Testcontainers. |
| Stored Procedure | `sp_evaluate_loan_application`, ejecutado por `AutomaticLoanEvaluationAdapter`. |
| Swagger | SpringDoc OpenAPI en `/swagger-ui.html` y `/v3/api-docs`. |
| Buenas prácticas | SOLID, DRY, KISS y YAGNI; separación por puertos/adaptadores, DTOs, validación de entrada, `BigDecimal` para dinero y repositorios abstraídos. |

## Funcionalidades

1. Registro y autenticación de clientes.
2. Registro de solicitudes de préstamo.
3. Consulta, búsqueda, filtro por estado, ordenamiento y paginación.
4. Aprobación o rechazo manual por personal autorizado.
5. Evaluación automática mediante Stored Procedure.
6. Creación del préstamo y generación del plan de pagos al aprobar.
7. Notificaciones simuladas para recepción, aprobación, revisión manual y rechazo.
8. Reporte del monto total de préstamos aprobados.
9. Administración de tipos de préstamo.

## Reglas funcionales

### Registro de clientes

- Nombres, apellidos, correo, tipo y número de documento, salario y contraseña son obligatorios cuando corresponda.
- El correo debe tener formato válido y ser único, sin distinguir mayúsculas/minúsculas.
- El salario debe estar en el intervalo `[0, 15.000.000]`.
- Las restricciones se aplican tanto con Bean Validation en la API como con restricciones PostgreSQL.

### Solicitudes

- Se valida que el cliente y el tipo de préstamo existan.
- El tipo de préstamo debe estar activo.
- Se validan monto mínimo/máximo y plazo permitido por el tipo de préstamo.
- El estado inicial es `PENDING_REVIEW`.

### Evaluación automática

La evaluación utiliza el procedimiento almacenado `sp_evaluate_loan_application` y aplica:

```text
CapacidadMáxima = SalarioBase × 0.35
DeudaActual = suma de cuotas mensuales de préstamos registrados
CapacidadDisponible = max(CapacidadMáxima - DeudaActual, 0)
```

La cuota se calcula con amortización francesa:

```text
Cuota = P × (i × (1 + i)^n) / ((1 + i)^n - 1)
```

Donde `P` es el monto, `i` la tasa mensual (`tasa anual / 100 / 12`) y `n` el plazo en meses. Para tasa cero se utiliza `P / n`.

La decisión es:

- `REJECTED`: la cuota supera la capacidad disponible.
- `MANUAL_REVIEW`: la cuota cabe, pero el monto supera cinco salarios base.
- `APPROVED`: cumple ambas condiciones.

Una aprobación genera el préstamo, sus cuotas y la notificación correspondiente.

## API principal

La API usa la base `/api/v1` y autenticación mediante cookies HttpOnly con tokens JWT.

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/auth/register` | Público | Registrar cliente con credenciales. |
| `POST` | `/auth/login` | Público | Iniciar sesión. |
| `POST` | `/auth/refresh` | Público | Renovar sesión. |
| `POST` | `/staff/login` | Público | Iniciar sesión como personal. |
| `POST` | `/customers` | Autenticado | Registrar cliente desde backoffice. |
| `POST` | `/loan-applications` | Autenticado | Crear solicitud. El cliente usa su propia identidad; staff puede enviar `customerId`. |
| `GET` | `/loan-applications` | Autenticado | Listar con `page`, `size`, `status`, `search`, `sortBy` y `sortDir`. |
| `GET` | `/loan-applications/{id}` | Autenticado | Consultar una solicitud. |
| `POST` | `/loan-applications/{id}/approve` | Staff | Aprobar manualmente. |
| `POST` | `/loan-applications/{id}/reject` | Staff | Rechazar manualmente con motivo. |
| `POST` | `/loan-applications/{id}/automatic-evaluation` | Staff | Ejecutar evaluación automática. |
| `GET` | `/loans` | Autenticado | Listar préstamos. |
| `GET` | `/loans/{id}/payment-plan` | Autenticado | Consultar plan de pagos. |
| `GET` | `/reports/approved-loans/total` | Admin/Staff | Obtener monto total y cantidad de préstamos aprobados. |

El contrato completo está disponible en Swagger.

## Arquitectura

```text
apps/backend/src/main/java/com/prestamosfacil/
├── domain/
│   ├── customer, loan, loanapplication, loantype, paymentplan, reporting
│   ├── shared/                 # Value Objects, paginación y excepciones
│   └── .../port/               # Puertos de entrada y salida
├── application/                # Casos de uso y orquestación
└── infrastructure/
    ├── adapter/in/rest/        # Controladores y DTOs
    ├── adapter/out/            # JPA, reportes, Stored Procedure y notificaciones
    ├── security/               # JWT, cookies, CSRF y rate limiting
    └── configuration/          # Beans y OpenAPI
```

Las migraciones Flyway se encuentran en `apps/backend/src/main/resources/db/migration`. El esquema final separa la identidad en `users` del perfil financiero y personal en `customers`, relacionado mediante `customers.user_id`. Las consultas de autenticación usan `users`; las consultas de clientes usan `customers`.

## Base de datos

El modelo final incluye, entre otras, estas tablas:

- `users`, `roles`, `user_roles` y `auth_tokens`.
- `customers` como perfil 1:1 opcional para usuarios con rol `CUSTOMER`.
- `document_types`, `loan_types` y `loan_application_statuses`.
- `loan_applications` y `loan_application_status_history`.
- `loans` y `payment_installments`.
- `outbox_events`.

Las solicitudes conservan su historial de estados. Cada fila de `loans` representa un préstamo creado después de una aprobación y contiene su cuota mensual; el esquema actual no conserva una columna de estado en `loans`.

## Ejecución local

### Dependencias

- Java 21+
- Docker y Docker Compose
- Node.js y pnpm

### Con Make

```bash
make up       # PostgreSQL y Mailpit
make api      # Backend en http://localhost:4010
make web      # Frontend en http://localhost:4000
make test-api # Tests del backend
make test-web # Tests del frontend
make down     # Detener servicios
```

### Comandos directos

```bash
docker compose up -d

cd apps/backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

cd ../frontend
pnpm install
pnpm test -- --run
```

Para ejecutar la verificación completa del backend:

```bash
cd apps/backend
./gradlew clean check --rerun-tasks
```

La prueba `AutomaticLoanEvaluationAdapterIntegrationTest` utiliza PostgreSQL real mediante Testcontainers. Si Docker no está disponible, Gradle la marca como omitida; en ese caso el resultado no certifica la ejecución real del Stored Procedure.

## URLs locales

- Frontend: <http://localhost:4000>
- API: <http://localhost:4010/api/v1>
- Swagger UI: <http://localhost:4010/swagger-ui.html>
- OpenAPI JSON: <http://localhost:4010/v3/api-docs>
- Mailpit: <http://localhost:4050>
- Diagrama relacional: <http://localhost:4000/diagrama-relacional>

## Credenciales demo

Las credenciales dependen del seed configurado en el entorno. Para desarrollo local se encuentran en las migraciones/seed y deben cambiarse antes de cualquier despliegue.

## Estado de calidad

- Las reglas de arquitectura se verifican con ArchUnit.
- Las reglas financieras tienen pruebas unitarias.
- Las validaciones de controladores tienen pruebas de integración REST.
- Los límites de cobertura se configuran en Gradle mediante JaCoCo.
- Los nombres de endpoints y la seguridad deben mantenerse alineados con los controladores reales.
