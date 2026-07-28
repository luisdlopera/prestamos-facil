# ADR-002: Arquitectura Hexagonal en el Backend

## Estado
Aceptado

## Contexto
El backend necesita ser mantenible, testeable y preparado para cambios de infraestructura (base de datos, proveedores de crédito, notificaciones).

## Decisión
Implementar arquitectura hexagonal (puertos y adaptadores) con capas:
- **Domain**: Entidades, Value Objects, puertos de salida. Sin dependencias de Spring/JPA.
- **Application**: Casos de uso. Depende solo del dominio.
- **Infrastructure**: Adaptadores de entrada (REST) y salida (JPA, procedures).

## Consecuencias
- El dominio es puramente Java, testeable sin Spring
- Cambiar de JPA a otro mecanismo de persistencia solo afecta infrastructure
- Se requiere mapeo entre entidades de dominio y entidades JPA
- Mayor número de clases (cada adaptador tiene su propio mapper)

## Reglas Verificables (ArchUnit)
- Domain no puede depender de Infrastructure
- Domain no puede depender de Spring
- Application no puede depender de adaptadores REST o persistencia
- Infrastructure puede depender de Application y Domain
