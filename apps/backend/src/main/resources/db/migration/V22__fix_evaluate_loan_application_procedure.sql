-- V20: Corrige sp_evaluate_loan_application y agrega el porcentaje de endeudamiento
-- Motivo:
--   1. V16 eliminó la columna loans.status (el ciclo de vida del préstamo quedó fuera
--      de alcance: todo registro en `loans` representa por definición una solicitud ya
--      aprobada). El predicado "AND l.status = 'ACTIVE'" de la versión original del SP
--      (V8) hacía referencia a una columna que ya no existe, por lo que el procedimiento
--      fallaría al ejecutarse contra el esquema actual.
--   2. Se agregan validaciones explícitas de los datos de entrada en vez de dejar que
--      un valor nulo/ inválido produzca un error de BD no controlado.
--   3. Se agrega el parámetro de salida p_debt_ratio (porcentaje de endeudamiento),
--      requerido por la API para mostrar una respuesta completa de la evaluación.

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
    -- Reiniciar salidas para evitar arrastrar valores de una llamada previa
    p_decision := NULL;
    p_max_capacity := NULL;
    p_current_debt := NULL;
    p_available_capacity := NULL;
    p_new_installment := NULL;
    p_debt_ratio := NULL;
    p_reason := NULL;

    -- Obtener los datos de la solicitud, el cliente y el tipo de préstamo
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

    -- Validación de parámetros de entrada
    IF v_base_salary IS NULL OR v_base_salary <= 0 THEN
        p_decision := 'REJECTED';
        p_reason := 'El salario base del solicitante es inválido';
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

    -- Capacidad máxima de endeudamiento: 35% de los ingresos totales
    p_max_capacity := ROUND(v_base_salary * 0.35, 2);

    -- Deuda mensual actual: suma de las cuotas de todos los préstamos vigentes del
    -- cliente. Cada fila en `loans` es, por diseño (post V16), un préstamo ya aprobado.
    SELECT COALESCE(SUM(l.monthly_payment), 0)
    INTO p_current_debt
    FROM loans l
    WHERE l.customer_id = v_customer_id;

    -- Capacidad disponible
    p_available_capacity := GREATEST(p_max_capacity - p_current_debt, 0);

    -- Cuota del nuevo préstamo (amortización francesa, tasa mensual simple = anual / 12)
    v_monthly_rate := (v_annual_rate / 12.0) / 100.0;

    IF v_monthly_rate = 0 THEN
        p_new_installment := ROUND(v_requested_amount / v_term_months, 2);
    ELSE
        v_factor := POWER(1 + v_monthly_rate, v_term_months);
        v_numerator := v_requested_amount * v_monthly_rate * v_factor;
        v_denominator := v_factor - 1;
        p_new_installment := ROUND(v_numerator / v_denominator, 2);
    END IF;

    -- Porcentaje de endeudamiento: deuda total (actual + cuota nueva) sobre el salario
    p_debt_ratio := ROUND(((p_current_debt + p_new_installment) / v_base_salary) * 100, 2);

    -- Precedencia de decisión (orden estricto)
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
