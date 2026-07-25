-- ============================================================
-- V32: Unify customers into users table, drop customers table,
--      and add base_salary integrity constraint [0, 15000000]
-- ============================================================

-- 1. Add profile columns to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(10),
    ADD COLUMN IF NOT EXISTS document_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS base_salary DECIMAL(15,2);

-- 2. Populate profile data from customers table if exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customers') THEN
        UPDATE users u
        SET first_name = COALESCE(c.first_name, SPLIT_PART(u.name, ' ', 1)),
            last_name = COALESCE(NULLIF(c.last_name, ''), NULLIF(SUBSTRING(u.name FROM POSITION(' ' IN u.name) + 1), ''), 'Sistema'),
            document_type = COALESCE(c.document_type, 'CC'),
            document_number = COALESCE(c.document_number, 'DOC-' || u.id),
            base_salary = COALESCE(c.base_salary, 0.00)
        FROM customers c
        WHERE c.user_id = u.id;
    END IF;
END $$;

-- 3. Fill defaults for any remaining users (staff/admin) without customer profile
UPDATE users
SET first_name = COALESCE(NULLIF(first_name, ''), SPLIT_PART(name, ' ', 1)),
    last_name = COALESCE(NULLIF(last_name, ''), NULLIF(SUBSTRING(name FROM POSITION(' ' IN name) + 1), ''), 'Sistema'),
    document_type = COALESCE(document_type, 'CC'),
    document_number = COALESCE(document_number, 'DOC-' || id),
    base_salary = COALESCE(base_salary, 0.00)
WHERE first_name IS NULL OR last_name IS NULL OR document_type IS NULL OR document_number IS NULL OR base_salary IS NULL;

-- 4. Apply NOT NULL constraints and DB Integrity rules
ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    ALTER COLUMN document_type SET NOT NULL,
    ALTER COLUMN document_number SET NOT NULL,
    ALTER COLUMN base_salary SET NOT NULL;

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS fk_users_document_type,
    DROP CONSTRAINT IF EXISTS uk_users_document_number,
    DROP CONSTRAINT IF EXISTS chk_users_base_salary;

ALTER TABLE users
    ADD CONSTRAINT fk_users_document_type FOREIGN KEY (document_type) REFERENCES document_types(code),
    ADD CONSTRAINT uk_users_document_number UNIQUE (document_number),
    ADD CONSTRAINT chk_users_base_salary CHECK (base_salary >= 0 AND base_salary <= 15000000.00);

-- 5. Re-point FKs in loan_applications & loans directly to users(id)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customers') THEN
        UPDATE loan_applications la
        SET customer_id = c.user_id
        FROM customers c
        WHERE la.customer_id = c.id;

        UPDATE loans l
        SET customer_id = c.user_id
        FROM customers c
        WHERE l.customer_id = c.id;
    END IF;
END $$;

ALTER TABLE loan_applications
    DROP CONSTRAINT IF EXISTS fk_loan_applications_customer;

ALTER TABLE loan_applications
    ADD CONSTRAINT fk_loan_applications_customer
        FOREIGN KEY (customer_id) REFERENCES users(id);

ALTER TABLE loans
    DROP CONSTRAINT IF EXISTS fk_loans_customer;

ALTER TABLE loans
    ADD CONSTRAINT fk_loans_customer
        FOREIGN KEY (customer_id) REFERENCES users(id);

-- 6. Drop redundant customers table
DROP TABLE IF EXISTS customers CASCADE;

-- 7. Update Stored Procedure sp_evaluate_loan_application to query users directly
CREATE OR REPLACE PROCEDURE sp_evaluate_loan_application(
    p_application_id UUID,
    INOUT p_decision VARCHAR(20),
    INOUT p_max_capacity DECIMAL(15,2),
    INOUT p_current_debt DECIMAL(15,2),
    INOUT p_available_capacity DECIMAL(15,2),
    INOUT p_new_installment DECIMAL(15,2),
    INOUT p_debt_ratio DECIMAL(6,2),
    INOUT p_reason VARCHAR(500)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_customer_id UUID;
    v_base_salary DECIMAL(15,2);
    v_requested_amount DECIMAL(15,2);
    v_term_months INT;
    v_annual_rate DECIMAL(5,2);
    v_monthly_rate DECIMAL(10,8);
    v_factor DECIMAL(20,10);
    v_numerator DECIMAL(20,10);
    v_denominator DECIMAL(20,10);
BEGIN
    -- Reset outputs
    p_decision := NULL;
    p_max_capacity := NULL;
    p_current_debt := NULL;
    p_available_capacity := NULL;
    p_new_installment := NULL;
    p_debt_ratio := NULL;
    p_reason := NULL;

    -- Get application, user (customer) and loan type data
    SELECT la.customer_id, u.base_salary, la.requested_amount, la.term_in_months, lt.annual_interest_rate
    INTO v_customer_id, v_base_salary, v_requested_amount, v_term_months, v_annual_rate
    FROM loan_applications la
    JOIN users u ON u.id = la.customer_id
    JOIN loan_types lt ON lt.id = la.loan_type_id
    WHERE la.id = p_application_id;

    IF NOT FOUND THEN
        p_decision := 'REJECTED';
        p_reason := 'Solicitud no encontrada';
        RETURN;
    END IF;

    -- Validate input parameters
    IF v_base_salary IS NULL OR v_base_salary < 0 OR v_base_salary > 15000000.00 THEN
        p_decision := 'REJECTED';
        p_reason := 'El salario base del solicitante es inválido (debe estar entre 0 y 15,000,000)';
        RETURN;
    END IF;

    IF v_requested_amount IS NULL OR v_requested_amount <= 0 THEN
        p_decision := 'REJECTED';
        p_reason := 'El monto solicitado es inválido';
        RETURN;
    END IF;

    IF v_term_months IS NULL OR v_term_months <= 0 THEN
        p_decision := 'REJECTED';
        p_reason := 'El plazo solicitado es inválido';
        RETURN;
    END IF;

    IF v_annual_rate IS NULL OR v_annual_rate < 0 THEN
        p_decision := 'REJECTED';
        p_reason := 'La tasa de interés del tipo de préstamo es inválida';
        RETURN;
    END IF;

    -- Maximum capacity: 35% of total income
    p_max_capacity := ROUND(v_base_salary * 0.35, 2);

    -- Current monthly debt: sum of active loans
    SELECT COALESCE(SUM(l.monthly_payment), 0)
    INTO p_current_debt
    FROM loans l
    WHERE l.customer_id = v_customer_id;

    -- Available capacity
    p_available_capacity := GREATEST(p_max_capacity - p_current_debt, 0);

    -- New installment calculation
    v_monthly_rate := (v_annual_rate / 12.0) / 100.0;

    IF v_monthly_rate = 0 THEN
        p_new_installment := ROUND(v_requested_amount / v_term_months, 2);
    ELSE
        v_factor := POWER(1 + v_monthly_rate, v_term_months);
        v_numerator := v_requested_amount * v_monthly_rate * v_factor;
        v_denominator := v_factor - 1;
        p_new_installment := ROUND(v_numerator / v_denominator, 2);
    END IF;

    -- Debt ratio
    p_debt_ratio := ROUND(((p_current_debt + p_new_installment) / NULLIF(v_base_salary, 0)) * 100, 2);

    -- Decision hierarchy
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
END;
$$;
