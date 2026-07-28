# Flujo de Evaluación Automática (Stored Procedure)

Recorrido completo archivo por archivo desde que un staff ejecuta la evaluación automática hasta que el stored procedure en PostgreSQL devuelve la decisión.

```
POST /api/v1/loan-applications/{id}/automatic-evaluation
```

---

## Diagrama de Arquitectura del Flujo

```
┌─────────────────────────────────────────────────────────────────────┐
│  CLIENTE (Staff)                                                    │
│  POST /api/v1/loan-applications/{id}/automatic-evaluation           │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE (inbound adapter)                                    │
│  📄 LoanApplicationController.java                                  │
│    → Recibe HTTP, llama al caso de uso                              │
│    → Depende de LoanApplicationUseCase (interfaz)                   │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│  APPLICATION (input port)                                            │
│  📄 application/port/in/LoanApplicationUseCase.java                  │
│    → Interfaz que define el contrato del caso de uso                │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│  APPLICATION (use case)                                              │
│  📄 loanapplication/LoanApplicationUseCaseImpl.java                  │
│    → Valida reglas de negocio                                       │
│    → Llama a AutomaticLoanEvaluationPort (interfaz)                 │
│    → Interpreta resultado: APPROVED / MANUAL_REVIEW / REJECTED      │
└──────────────┬───────────────────────────────────┬──────────────────┘
               │                                   │
               ▼                                   ▼
┌──────────────────────────────┐    ┌─────────────────────────────────┐
│  DOMAIN (output port)        │    │  DOMAIN (entidades)             │
│  📄 domain/port/out/         │    │  📄 loanapplication/            │
│    AutomaticLoanEvaluationPort│   │    LoanApplication.java         │
│    → Interfaz de negocio     │    │    → approve(), reject(),       │
│    → define el QUÉ (evaluar) │    │      markForManualReview()      │
└──────────────┬───────────────┘    └─────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE (outbound adapter)                                   │
│  📄 storedprocedure/PostgresAutomaticLoanEvaluationAdapter.java      │
│    → Implementa AutomaticLoanEvaluationPort                         │
│    → Traduce a CALL sp_evaluate_loan_application(...) vía JPA       │
│    → Define el CÓMO (PostgreSQL stored procedure)                   │
└──────────────┬──────────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────────┐
│  DATABASE (Flyway migration)                                        │
│  📄 resources/db/migration/V8__create_evaluation_procedure.sql       │
│    → CREATE OR REPLACE PROCEDURE sp_evaluate_loan_application(...)  │
│    → Lógica de evaluación en PL/pgSQL                               │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 1. Capa de Infraestructura — Controller

```
infrastructure/adapter/in/rest/LoanApplicationController.java
```

Recibe la petición HTTP, extrae el ID de la solicitud y delega en el caso de uso. No contiene lógica de negocio.

```java
@PostMapping("/{id}/automatic-evaluation")
public ResponseEntity<AutomaticEvaluationResponse> automaticEvaluation(@PathVariable UUID id) {
    LoanApplicationStatus status = loanApplicationUseCase.evaluateAutomatically(id);
    LoanApplication application = loanApplicationUseCase.findById(id).orElseThrow();
    return ResponseEntity.ok(new AutomaticEvaluationResponse(
        id, status.name(), application.getDecisionReason()));
}
```

**Dependencia:** `LoanApplicationUseCase` (interfaz en `application/port/in/`)

---

## 2. Capa de Aplicación — Input Port

```
application/port/in/LoanApplicationUseCase.java
```

Interfaz que define los contratos de los casos de uso relacionados con solicitudes de préstamo. El controller depende de esta interfaz, no de la implementación concreta.

```java
public interface LoanApplicationUseCase {
    LoanApplicationStatus evaluateAutomatically(UUID applicationId);
    // ... otros métodos
}
```

---

## 3. Capa de Aplicación — Use Case

```
application/loanapplication/LoanApplicationUseCaseImpl.java
```

Contiene la lógica de orquestación. Valida las reglas de negocio antes de llamar al puerto de salida:

```java
@Override
@Transactional
public LoanApplicationStatus evaluateAutomatically(UUID applicationId) {
    LoanApplication application = loanApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new EntityNotFoundException("..."));
```

### Validaciones de negocio

| Regla | Código |
|---|---|
| Solo solicitudes en `PENDING_REVIEW` | `application.getStatus() != LoanApplicationStatus.PENDING_REVIEW` |
| Tipo de préstamo debe tener evaluación automática habilitada | `application.getLoanType().isAutomaticValidationEnabled()` |

### Llamada al puerto de salida

```java
    AutomaticLoanEvaluationPort.EvaluationResult evalResult = evaluationPort.evaluate(
        applicationId,
        customer.getBaseSalary().getAmount(),
        application.getRequestedAmount().getAmount(),
        application.getTermInMonths(),
        application.getLoanType().getAnnualInterestRate());

    return applyAutomaticDecision(application, evalResult);
}
```

### Interpretación de la decisión

```java
private LoanApplicationStatus applyAutomaticDecision(
        LoanApplication application,
        AutomaticLoanEvaluationPort.EvaluationResult evalResult) {

    switch (evalResult.decision()) {
        case "APPROVED" -> {
            application.setMonthlyPayment(new Money(evalResult.newInstallment()));
            application.setDecisionReason(evalResult.reason());
            approveWithPaymentPlan(application, monthlyPayment);
            return LoanApplicationStatus.APPROVED;
        }
        case "MANUAL_REVIEW" -> {
            application.setDecisionReason(evalResult.reason());
            application.markForManualReview();
            loanApplicationRepository.save(application);
            return LoanApplicationStatus.MANUAL_REVIEW;
        }
        case "REJECTED" -> {
            application.reject(evalResult.reason());
            loanApplicationRepository.save(application);
            return LoanApplicationStatus.REJECTED;
        }
    }
}
```

**Dependencia:** `AutomaticLoanEvaluationPort` (interfaz en `domain/port/out/`)

---

## 4. Capa de Dominio — Output Port

```
domain/port/out/AutomaticLoanEvaluationPort.java
```

Puerto de salida que define el contrato para evaluar una solicitud. Está en la capa de dominio porque expresa una **necesidad del negocio**: evaluar capacidad crediticia. No importa si la implementación es un stored procedure, un motor de reglas o un servicio externo.

```java
package com.prestamosfacil.domain.port.out;

public interface AutomaticLoanEvaluationPort {

    EvaluationResult evaluate(UUID loanApplicationId, BigDecimal baseSalary,
                              BigDecimal requestedAmount, int termInMonths,
                              BigDecimal annualInterestRate);

    record EvaluationResult(
            String decision,
            BigDecimal maxCapacity,
            BigDecimal currentDebt,
            BigDecimal availableCapacity,
            BigDecimal newInstallment,
            String reason) {}
}
```

### Atributos del resultado

| Campo | Tipo | Descripción |
|---|---|---|
| `decision` | `String` | `APPROVED`, `MANUAL_REVIEW` o `REJECTED` |
| `maxCapacity` | `BigDecimal` | 35 % del salario base |
| `currentDebt` | `BigDecimal` | Suma de cuotas de préstamos activos |
| `availableCapacity` | `BigDecimal` | Capacidad máxima - deuda actual (mínimo 0) |
| `newInstallment` | `BigDecimal` | Cuota mensual calculada (amortización francesa) |
| `reason` | `String` | Motivo de la decisión |

---

## 5. Capa de Infraestructura — Adapter del Stored Procedure

```
infrastructure/adapter/out/storedprocedure/PostgresAutomaticLoanEvaluationAdapter.java
```

Implementa el puerto de dominio. Es el único lugar del sistema que sabe que la evaluación se hace mediante un stored procedure de PostgreSQL. Usa JPA `StoredProcedureQuery` para mapear la llamada.

```java
@Component
public class PostgresAutomaticLoanEvaluationAdapter implements AutomaticLoanEvaluationPort {

    private static final String PROCEDURE_NAME = "sp_evaluate_loan_application";
    private static final Logger log = LoggerFactory.getLogger(PostgresAutomaticLoanEvaluationAdapter.class);
    private final EntityManager entityManager;

    @Override
    public EvaluationResult evaluate(UUID loanApplicationId, BigDecimal baseSalary,
                                      BigDecimal requestedAmount, int termInMonths,
                                      BigDecimal annualInterestRate) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(PROCEDURE_NAME);

        query.registerStoredProcedureParameter("p_application_id", UUID.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_decision", String.class, ParameterMode.INOUT);
        query.registerStoredProcedureParameter("p_max_capacity", BigDecimal.class, ParameterMode.INOUT);
        query.registerStoredProcedureParameter("p_current_debt", BigDecimal.class, ParameterMode.INOUT);
        query.registerStoredProcedureParameter("p_available_capacity", BigDecimal.class, ParameterMode.INOUT);
        query.registerStoredProcedureParameter("p_new_installment", BigDecimal.class, ParameterMode.INOUT);
        query.registerStoredProcedureParameter("p_reason", String.class, ParameterMode.INOUT);

        query.setParameter("p_application_id", loanApplicationId);
        query.setParameter("p_decision", "");
        // ... valores por defecto para INOUTs

        query.execute();

        String decision = (String) query.getOutputParameterValue("p_decision");
        BigDecimal maxCapacity = (BigDecimal) query.getOutputParameterValue("p_max_capacity");
        BigDecimal currentDebt = (BigDecimal) query.getOutputParameterValue("p_current_debt");
        BigDecimal availableCapacity = (BigDecimal) query.getOutputParameterValue("p_available_capacity");
        BigDecimal newInstallment = (BigDecimal) query.getOutputParameterValue("p_new_installment");
        String reason = (String) query.getOutputParameterValue("p_reason");

        return new EvaluationResult(
            decision, maxCapacity, currentDebt,
            availableCapacity, newInstallment, reason);
    }
}
```

---

## 6. Base de Datos — Stored Procedure (Flyway)

```
resources/db/migration/V8__create_evaluation_procedure.sql
```

Definición del stored procedure en PostgreSQL. Migrado automáticamente por Flyway al iniciar la aplicación.

### Firma

```sql
CREATE OR REPLACE PROCEDURE sp_evaluate_loan_application(
    p_application_id UUID,
    INOUT p_decision VARCHAR(20),
    INOUT p_max_capacity DECIMAL(15,2),
    INOUT p_current_debt DECIMAL(15,2),
    INOUT p_available_capacity DECIMAL(15,2),
    INOUT p_new_installment DECIMAL(15,2),
    INOUT p_reason VARCHAR(500)
)
LANGUAGE plpgsql
```

### Lógica paso a paso

| Paso | Operación | Fórmula |
|---|---|---|
| 1 | Obtener datos | `SELECT base_salary, requested_amount, term, rate FROM customer + application + loan_type` |
| 2 | Capacidad máxima | `salario_base × 0.35` |
| 3 | Deuda actual | `SUM(cuotas de préstamos ACTIVOS del cliente)` |
| 4 | Capacidad disponible | `MAX(capacidad_máxima - deuda_actual, 0)` |
| 5 | Cuota mensual | `P × r × (1+r)ⁿ / ((1+r)ⁿ − 1)` (amortización francesa) |

### Reglas de decisión (orden estricto)

```sql
IF p_new_installment > p_available_capacity THEN
    p_decision := 'REJECTED';
    p_reason := 'La cuota mensual supera la capacidad de endeudamiento disponible';
ELSIF v_requested_amount > v_base_salary * 5 THEN
    p_decision := 'MANUAL_REVIEW';
    p_reason := 'El monto solicitado supera 5 veces el salario base';
ELSE
    p_decision := 'APPROVED';
    p_reason := 'La solicitud cumple todos los criterios de evaluación automática';
END IF;
```

### Tabla de decisiones

| Condición | Resultado |
|---|---|
| Cuota nueva > Capacidad disponible | `REJECTED` |
| Cuota nueva ≤ Capacidad disponible Y monto > salario × 5 | `MANUAL_REVIEW` |
| Cuota nueva ≤ Capacidad disponible Y monto ≤ salario × 5 | `APPROVED` |

---

## Resumen de Dependencias

```
LoanApplicationController
    ↓ depende de interfaz
LoanApplicationUseCase (application/port/in/)
    ↓
LoanApplicationUseCaseImpl (application/)
    ↓ depende de interfaz        ↓ usa entidad de dominio
AutomaticLoanEvaluationPort   LoanApplication (domain/)
(domain/port/out/)
    ↓ implementa
PostgresAutomaticLoanEvaluationAdapter (infrastructure/)
    ↓ llama
sp_evaluate_loan_application (PostgreSQL / Flyway V8)
```

**Principio hexagonal:** El dominio (`AutomaticLoanEvaluationPort`) define el *qué* (evaluar una solicitud). La infraestructura (`PostgresAutomaticLoanEvaluationAdapter`) define el *cómo* (stored procedure en PostgreSQL). Si el stored procedure se reemplaza por un motor de reglas externo o un microservicio, solo cambia el adapter. El dominio y el caso de uso permanecen intactos.

---

## Archivos involucrados (rutas relativas a `apps/backend/src/main/java/com/prestamosfacil/`)

| Capa | Archivo | Propósito |
|---|---|---|
| Infrastructure | `infrastructure/adapter/in/rest/LoanApplicationController.java` | Controller REST |
| Application | `application/port/in/LoanApplicationUseCase.java` | Input port (interfaz) |
| Application | `application/loanapplication/LoanApplicationUseCaseImpl.java` | Use case (implementación) |
| Domain | `domain/port/out/AutomaticLoanEvaluationPort.java` | Output port (interfaz) |
| Infrastructure | `infrastructure/adapter/out/storedprocedure/PostgresAutomaticLoanEvaluationAdapter.java` | Adapter del SP |
| Database | `resources/db/migration/V8__create_evaluation_procedure.sql` | Definición del SP |
