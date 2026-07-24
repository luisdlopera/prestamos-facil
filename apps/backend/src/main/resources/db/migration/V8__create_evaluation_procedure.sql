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
    v_active_loans_count INT;
BEGIN
    -- Get application data
    SELECT la.customer_id, c.base_salary, la.requested_amount, la.term_in_months, lt.annual_interest_rate
    INTO v_customer_id, v_base_salary, v_requested_amount, v_term_months, v_annual_rate
    FROM loan_applications la
    JOIN customers c ON c.id = la.customer_id
    JOIN loan_types lt ON lt.id = la.loan_type_id
    WHERE la.id = p_application_id;

    IF NOT FOUND THEN
        p_decision := 'REJECTED';
        p_reason := 'Solicitud no encontrada';
        RETURN;
    END IF;

    -- Calculate capacity
    p_max_capacity := v_base_salary * 0.35;

    -- Calculate current debt from active loans
    SELECT COALESCE(SUM(l.monthly_payment), 0)
    INTO p_current_debt
    FROM loans l
    WHERE l.customer_id = v_customer_id
      AND l.status = 'ACTIVE';

    -- Calculate available capacity
    p_available_capacity := GREATEST(p_max_capacity - p_current_debt, 0);

    -- Calculate new installment using French amortization
    v_monthly_rate := (v_annual_rate / 12.0) / 100.0;

    IF v_monthly_rate = 0 THEN
        p_new_installment := ROUND(v_requested_amount / v_term_months, 2);
    ELSE
        v_factor := POWER(1 + v_monthly_rate, v_term_months);
        v_numerator := v_requested_amount * v_monthly_rate * v_factor;
        v_denominator := v_factor - 1;
        p_new_installment := ROUND(v_numerator / v_denominator, 2);
    END IF;

    -- Decision precedence (strict order)
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
