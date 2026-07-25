-- V34: Separate customer profile data from authentication identity.
-- The migration is forward-only and preserves existing user ids so existing
-- customer-facing references remain stable.

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(50) NOT NULL UNIQUE,
    base_salary DECIMAL(15,2) NOT NULL,
    phone VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_customers_document_type FOREIGN KEY (document_type) REFERENCES document_types(code),
    CONSTRAINT chk_customers_base_salary CHECK (base_salary >= 0 AND base_salary <= 15000000.00)
);

INSERT INTO customers (id, user_id, first_name, last_name, document_type, document_number, base_salary)
SELECT id,
       id,
       first_name,
       last_name,
       document_type,
       document_number,
       base_salary
FROM users
WHERE UPPER(role) = 'CUSTOMER';

CREATE INDEX idx_customers_user_id ON customers(user_id);
CREATE INDEX idx_customers_name ON customers(first_name, last_name);
CREATE INDEX idx_customers_document_number ON customers(document_number);

ALTER TABLE loan_applications DROP CONSTRAINT IF EXISTS fk_loan_applications_customer;
ALTER TABLE loans DROP CONSTRAINT IF EXISTS fk_loans_customer;

ALTER TABLE loan_applications
    ADD CONSTRAINT fk_loan_applications_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE loans
    ADD CONSTRAINT fk_loans_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE users
    DROP COLUMN first_name,
    DROP COLUMN last_name,
    DROP COLUMN document_type,
    DROP COLUMN document_number,
    DROP COLUMN base_salary,
    DROP COLUMN name;

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
BEGIN
    p_decision := NULL; p_max_capacity := NULL; p_current_debt := NULL;
    p_available_capacity := NULL; p_new_installment := NULL;
    p_debt_ratio := NULL; p_reason := NULL;

    SELECT la.customer_id, c.base_salary, la.requested_amount,
           la.term_in_months, lt.annual_interest_rate
    INTO v_customer_id, v_base_salary, v_requested_amount,
         v_term_months, v_annual_rate
    FROM loan_applications la
    JOIN customers c ON c.id = la.customer_id
    JOIN loan_types lt ON lt.id = la.loan_type_id
    WHERE la.id = p_application_id;

    IF NOT FOUND THEN
        p_decision := 'REJECTED'; p_reason := 'Solicitud no encontrada'; RETURN;
    END IF;
    IF v_base_salary IS NULL OR v_base_salary < 0 OR v_base_salary > 15000000.00 THEN
        p_decision := 'REJECTED'; p_reason := 'El salario base del solicitante es inválido'; RETURN;
    END IF;
    IF v_requested_amount IS NULL OR v_requested_amount <= 0 THEN
        p_decision := 'REJECTED'; p_reason := 'El monto solicitado es inválido'; RETURN;
    END IF;
    IF v_term_months IS NULL OR v_term_months <= 0 THEN
        p_decision := 'REJECTED'; p_reason := 'El plazo solicitado es inválido'; RETURN;
    END IF;
    IF v_annual_rate IS NULL OR v_annual_rate < 0 THEN
        p_decision := 'REJECTED'; p_reason := 'La tasa de interés es inválida'; RETURN;
    END IF;

    p_max_capacity := ROUND(v_base_salary * 0.35, 2);
    SELECT COALESCE(SUM(l.monthly_payment), 0)
    INTO p_current_debt FROM loans l WHERE l.customer_id = v_customer_id;
    p_available_capacity := GREATEST(p_max_capacity - p_current_debt, 0);
    v_monthly_rate := (v_annual_rate / 12.0) / 100.0;

    IF v_monthly_rate = 0 THEN
        p_new_installment := ROUND(v_requested_amount / v_term_months, 2);
    ELSE
        v_factor := POWER(1 + v_monthly_rate, v_term_months);
        p_new_installment := ROUND(
            v_requested_amount * v_monthly_rate * v_factor / (v_factor - 1), 2);
    END IF;

    p_debt_ratio := ROUND(
        ((p_current_debt + p_new_installment) / NULLIF(v_base_salary, 0)) * 100, 2);

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
