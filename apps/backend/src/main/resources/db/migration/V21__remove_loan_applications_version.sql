-- V19: Eliminar columna version de loan_applications
-- Rationale: El control de concurrencia optimista vía versión ya no es necesario
-- en la tabla de solicitudes de préstamo.

ALTER TABLE loan_applications
    DROP COLUMN IF EXISTS version;
