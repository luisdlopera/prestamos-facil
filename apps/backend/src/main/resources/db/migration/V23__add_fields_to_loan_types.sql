-- Migration V21: Add description, rate_type, and display_order to loan_types

ALTER TABLE loan_types
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS rate_type VARCHAR(10) NOT NULL DEFAULT 'EA',
    ADD COLUMN IF NOT EXISTS display_order INT NOT NULL DEFAULT 0;

-- Update existing loan types with descriptions and display orders
UPDATE loan_types SET display_order = 1, rate_type = 'EA', description = 'Crédito de libre destinación para libre inversión personal o familiar.' WHERE name = 'Libre Inversión';
UPDATE loan_types SET display_order = 2, rate_type = 'EA', description = 'Financiamiento para adquisición o remodelación de vivienda.' WHERE name = 'Vivienda';
UPDATE loan_types SET display_order = 3, rate_type = 'EA', description = 'Crédito educativo para cubrir matrículas y estudios superiores.' WHERE name = 'Educativo';
UPDATE loan_types SET display_order = 4, rate_type = 'EA', description = 'Financiamiento para la compra de vehículo nuevo o usado.' WHERE name = 'Compra de Vehículo';
UPDATE loan_types SET display_order = 5, rate_type = 'EA', description = 'Microcrédito destinado a capital de trabajo o pequeñas empresas.' WHERE name = 'Microcrédito';
