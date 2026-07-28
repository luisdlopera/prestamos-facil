# Modelo Relacional de Base de Datos (Notación Pata de Gallina / Crow's Foot)

Este documento detalla el modelo relacional de la base de datos PostgreSQL de **Préstamos Fácil**, estructurado a través de migraciones de Flyway (V1 - V23).

> [!NOTE]
> Se dispone de una herramienta interactiva de visualización del diagrama en la ruta: `/diagrama-relacional`.

---

## 1. Módulos y Tablas del Sistema

### 1.1 Módulo de Identidad & Seguridad
- **`users`**: Identidad y autenticación con columna `role` (CUSTOMER/ADMIN/STAFF/ANALYST), sin datos financieros o personales de cliente (PK: `id`, UNIQUE: `email`).
- **`roles`**: Catálogo de roles (PK: `id`).
- **`user_roles`**: Tabla intermedia N:M entre usuarios y roles (PK: `user_id`, `role_id`).
- **`auth_tokens`**: Unifica refresh_tokens + password_reset_tokens (PK: `id`, FK: `user_id`, type: REFRESH/PASSWORD_RESET).

### 1.2 Módulo de Referencia
- **`document_types`**: Catálogo de tipos de documento de identidad (PK: `code`).
- **`loan_types`**: Modalidades de crédito ofrecidas con tasas de interés y límites (PK: `id`).
- **`loan_application_statuses`**: Estados del ciclo de evaluación de solicitudes (PK: `code`).

### 1.3 Módulo de Clientes
- **`customers`**: Perfil financiero e información personal del cliente (PK: `id`, FK: `user_id` [1:1], FK: `document_type`). Solo existe para usuarios con rol `CUSTOMER`.

### 1.4 Módulo de Préstamos
- **`loan_applications`**: Solicitudes de préstamo radicadas y su resultado de evaluación (PK: `id`, FKs: `customer_id`, `loan_type_id`, `status`).
- **`loans`**: Créditos aprobados y activos en amortización (PK: `id`, FKs: `loan_application_id` [UNIQUE], `customer_id`, `approved_by`).
- **`payment_installments`**: Tabla de amortización con cuotas mensuales proyectadas (PK: `id`, FK: `loan_id`).
- **`loan_application_status_history`**: Historial de cambios de estado de solicitudes (PK: `id`, FK: `loan_application_id`).

---

## 2. Matriz de Relaciones y Cardinalidades

| Tabla Origen (PK / Padre) | Tabla Destino (FK / Hijo) | Llave Foránea (`FK`) | Cardinalidad | Descripción |
| :--- | :--- | :--- | :---: | :--- |
| `users` | `customers` | `user_id` | `1 : 0..1` | Un usuario posee opcionalmente 1 perfil de cliente. |
| `users` | `auth_tokens` | `user_id` | `1 : 0..N` | Un usuario posee 0 o más tokens de autenticación/sesión. |
| `users` | `user_roles` | `user_id` | `1 : 0..N` | Un usuario posee 0 o más asignaciones de rol. |
| `roles` | `user_roles` | `role_id` | `1 : 0..N` | Un rol se asigna a 0 o más usuarios. |
| `document_types` | `customers` | `document_type` | `1 : 0..N` | Un tipo de documento aplica a 0 o más clientes. |
| `customers` | `loan_applications` | `customer_id` | `1 : 0..N` | Un cliente radica 0 o más solicitudes de préstamo. |
| `loan_types` | `loan_applications` | `loan_type_id` | `1 : 0..N` | Un tipo de préstamo clasifica a 0 o más solicitudes. |
| `loan_application_statuses` | `loan_applications` | `status` | `1 : 0..N` | Un estado clasifica a 0 o más solicitudes. |
| `loan_applications` | `loans` | `loan_application_id` | `1 : 0..1` | Una solicitud aprobada produce máximo 1 préstamo. |
| `customers` | `loans` | `customer_id` | `1 : 0..N` | Un cliente posee 0 o más préstamos activos. |
| `users` | `loans` | `approved_by` | `0..1 : 0..N` | Un usuario staff aprueba opcionalmente 0 o más préstamos. |
| `loans` | `payment_installments` | `loan_id` | `1 : 1..N` | Un préstamo genera 1 o más cuotas de pago. |
| `loan_applications` | `loan_application_status_history` | `loan_application_id` | `1 : 0..N` | Una solicitud registra 0 o más entradas de historial de estado. |
| `loan_application_statuses` | `loan_application_status_history` | `status` | `1 : 0..N` | Un estado referencia 0 o más entradas de historial. |
| `users` | `loan_application_status_history` | `evaluated_by` | `0..1 : 0..N` | Un usuario evalúa opcionalmente 0 o más transiciones de estado. |

---

## 3. Diagrama interactivo

Para explorar interactivamente las entidades, filtros por módulo y líneas de conexión con notación Pata de Gallina, acceda a la URL:
`http://localhost:4000/diagrama-relacional`
