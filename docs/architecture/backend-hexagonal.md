# Arquitectura Hexagonal del Backend

## Principios

La dirección de dependencias es:

```
Infrastructure → Application → Domain
```

- **Domain**: No depende de Spring, JPA, Jackson ni ninguna librería externa.
- **Application**: Depende solo del dominio. Implementa casos de uso.
- **Infrastructure**: Implementa puertos de salida y expone adaptadores de entrada.

## Flujo de Dependencias

```
[REST adapter]        ← Adaptador de entrada (Controller)
     ↓
[Input port]          ← Interfaz de caso de uso (Application)
     ↓
[Application service] ← Implementación del caso de uso
     ↓
[Domain]              ← Entidades, Value Objects, reglas de negocio
     ↓
[Output port]         ← Interfaz de repositorio (Domain)
     ↓
[Persistence adapter] ← Implementación JPA (Infrastructure)
```

## Estructura de Paquetes

```
com.prestamosfacil
├── domain/                  # Núcleo del negocio
│   ├── customer/            # Clientes
│   ├── loantype/            # Tipos de préstamo
│   ├── loanapplication/     # Solicitudes
│   ├── loan/                # Préstamos
│   ├── paymentplan/         # Plan de pagos
│   ├── evaluation/          # Evaluación crediticia
│   ├── reporting/           # Puertos de reportes
│   ├── notification/        # Puertos de notificación
│   ├── auth/                # Autenticación
│   └── shared/              # Value Objects genéricos
│
├── application/             # Casos de uso
│   ├── customer/            # Servicios de cliente
│   ├── loantype/            # Servicios de tipo de préstamo
│   ├── loanapplication/     # Servicios de solicitud
│   ├── loan/                # Servicios de préstamo
│   ├── reporting/           # Casos de uso de reportes
│   └── auth/                # Servicios de autenticación
│
└── infrastructure/          # Adaptadores
    ├── configuration/       # Configuración Spring
    ├── adapter/
    │   ├── in/
    │   │   └── rest/        # Controladores REST
    │   └── out/
    │       ├── persistence/postgres/  # Repositorios JPA
    │       ├── procedure/postgres/    # Stored procedures
    │       └── notification/          # Notificaciones
    └── shared/
        └── rest/            # Manejadores globales de errores
```

## Canónicos Compartidos

Todas las respuestas HTTP siguen el formato `ApiResponse<T>`:

```json
{
  "data": { ... },
  "message": "Operación exitosa",
  "timestamp": "2026-07-24T12:00:00Z"
}
```

Respuestas paginadas:

```json
{
  "data": [ ... ],
  "message": "Registros recuperados exitosamente",
  "timestamp": "...",
  "pagination": {
    "page": 1,
    "perPage": 20,
    "total": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

Errores:

```json
{
  "data": null,
  "message": "Error de validación",
  "timestamp": "...",
  "errors": [
    { "code": "NOT_BLANK", "message": "must not be blank", "field": "firstName" }
  ]
}
```
