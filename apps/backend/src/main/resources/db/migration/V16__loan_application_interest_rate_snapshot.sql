-- V15: Snapshot de tasa de interés en solicitudes y eliminación de monthly_payment
-- Rationale: La tasa de interés se debe capturar en el momento de la solicitud (snapshot)
-- para que cambios futuros en loan_types no alteren el registro histórico.
-- monthly_payment no pertenece a loan_applications; es un resultado calculado
-- que se transfiere exclusivamente al préstamo (loans) al momento de aprobación.

ALTER TABLE loan_applications
    ADD COLUMN annual_interest_rate DECIMAL(5, 2) NOT NULL DEFAULT 0;

ALTER TABLE loan_applications
    DROP COLUMN IF EXISTS monthly_payment;

-- El DEFAULT 0 fue necesario para ALTER en registros existentes.
-- Lo removemos para forzar valor explícito en inserciones futuras.
ALTER TABLE loan_applications
    ALTER COLUMN annual_interest_rate DROP DEFAULT;
