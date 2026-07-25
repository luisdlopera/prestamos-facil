-- V17: Tabla intermedia loan_application_status_history
-- Rationale: En lugar de guardar el estado directamente en loan_applications,
-- se crea una tabla de historial que registra cada transición de estado con
-- opened_at / closed_at. El estado activo es el único con closed_at IS NULL.
-- Esto permite trazabilidad completa del ciclo de vida de cada solicitud.

-- 1. Crear la tabla de historial de estados
CREATE TABLE loan_application_status_history (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id  UUID         NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    decision_reason      VARCHAR(500),
    evaluated_by         UUID,
    opened_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    closed_at            TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_loan_app_status_history PRIMARY KEY (id),
    CONSTRAINT fk_lash_loan_application
        FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_lash_status
        FOREIGN KEY (status) REFERENCES loan_application_statuses(code),
    CONSTRAINT fk_lash_evaluated_by
        FOREIGN KEY (evaluated_by) REFERENCES users(id)
);

-- Índice para la búsqueda del estado activo (closed_at IS NULL)
CREATE INDEX idx_lash_active_status
    ON loan_application_status_history (loan_application_id)
    WHERE closed_at IS NULL;

-- Índice para recuperar el historial completo de una solicitud
CREATE INDEX idx_lash_loan_application_opened
    ON loan_application_status_history (loan_application_id, opened_at DESC);

-- 2. Migrar el estado actual de cada solicitud al historial
--    opened_at = created_at de la solicitud para preservar la cronología
INSERT INTO loan_application_status_history (
    id,
    loan_application_id,
    status,
    decision_reason,
    evaluated_by,
    opened_at,
    closed_at
)
SELECT
    gen_random_uuid(),
    la.id,
    la.status,
    la.decision_reason,
    la.evaluated_by,
    COALESCE(la.evaluated_at, la.created_at),
    NULL  -- closed_at NULL = estado activo actual
FROM loan_applications la;

-- 3. Eliminar las columnas que se movieron al historial
ALTER TABLE loan_applications
    DROP COLUMN IF EXISTS status,
    DROP COLUMN IF EXISTS decision_reason,
    DROP COLUMN IF EXISTS evaluated_at,
    DROP COLUMN IF EXISTS evaluated_by;
