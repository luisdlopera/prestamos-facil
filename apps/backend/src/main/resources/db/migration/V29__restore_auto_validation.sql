-- Restaura validación automática para los tipos que la tenían habilitada originalmente,
-- desactivada temporalmente en V28 para diagnóstico del DataIntegrityViolationException
UPDATE loan_types SET automatic_validation_enabled = true
WHERE name IN ('Libre Inversión', 'Educativo', 'Compra de Vehículo', 'Microcrédito');
