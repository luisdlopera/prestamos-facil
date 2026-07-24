ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS base_salary_check BOOLEAN,
    DROP CONSTRAINT IF EXISTS chk_customers_base_salary;

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_base_salary
        CHECK (base_salary >= 0 AND base_salary <= 15000000);

DROP INDEX IF EXISTS uk_customers_document_type_number;
CREATE UNIQUE INDEX uk_customers_document_type_number
    ON customers (document_type, document_number);

DROP INDEX IF EXISTS uk_users_email_lower;
CREATE UNIQUE INDEX uk_users_email_lower
    ON users (LOWER(email));

ALTER TABLE loan_types
    ADD COLUMN IF NOT EXISTS minimum_term_months INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS maximum_term_months INT NOT NULL DEFAULT 60;

UPDATE loan_types SET minimum_term_months = 6, maximum_term_months = 60 WHERE name = 'Libre Inversión';
UPDATE loan_types SET minimum_term_months = 12, maximum_term_months = 240 WHERE name = 'Vivienda';
UPDATE loan_types SET minimum_term_months = 6, maximum_term_months = 36 WHERE name = 'Educativo';
UPDATE loan_types SET minimum_term_months = 12, maximum_term_months = 84 WHERE name = 'Compra de Vehículo';
UPDATE loan_types SET minimum_term_months = 3, maximum_term_months = 24 WHERE name = 'Microcrédito';
