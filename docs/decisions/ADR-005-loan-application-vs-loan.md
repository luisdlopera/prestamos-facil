# ADR-005: Distinción entre LoanApplication y Loan

## Estado
Aceptado

## Contexto
El proceso de crédito tiene dos momentos claramente diferenciados: la solicitud (cuando el cliente pide un préstamo) y el préstamo (cuando es aprobado y desembolsado). Tratarlos como una sola entidad causa problemas.

## Decisión
Modelar dos entidades separadas:

- **LoanApplication**: Solicitud pendiente de evaluación. Puede estar PENDING, MANUAL_REVIEW, APPROVED o REJECTED.
- **Loan**: Préstamo creado solo después de una aprobación. Tiene su propio ciclo de vida (ACTIVE, PAID, DEFAULTED, WRITTEN_OFF).

## Consecuencias
- No se crea un préstamo al registrar una solicitud
- Una solicitud aprobada genera exactamente un préstamo (relación 1:0..1)
- Cada entidad tiene su propio flujo de estados
- La evaluación crediticia se aplica a la solicitud, no al préstamo
- El plan de pagos (payment_installments) pertenece al préstamo

## Alternativas Consideradas
- **Entidad única con campo de estado**: Mezcla dos ciclos de vida diferentes
- **Herencia de estado**: Complejidad innecesaria
