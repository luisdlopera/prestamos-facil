-- Desactiva validación automática en todos los tipos de préstamo para diagnosticar
-- si el DataIntegrityViolationException está relacionado con el flush del stored procedure.
-- Reversible con: UPDATE loan_types SET automatic_validation_enabled = prev_value WHERE name = '...';
UPDATE loan_types SET automatic_validation_enabled = false;
