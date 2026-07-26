package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan;

import com.prestamosfacil.domain.loanapplication.models.EvaluationResult;
import com.prestamosfacil.domain.loanapplication.port.out.AutomaticLoanEvaluationPort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AutomaticLoanEvaluationAdapter implements AutomaticLoanEvaluationPort {

    private static final Logger log = LoggerFactory.getLogger(AutomaticLoanEvaluationAdapter.class);
    private static final String PROCEDURE_NAME = "sp_evaluate_loan_application";

    private final EntityManager entityManager;

    public AutomaticLoanEvaluationAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EvaluationResult evaluate(UUID loanApplicationId, BigDecimal baseSalary,
                                     BigDecimal requestedAmount, int termInMonths,
                                     BigDecimal annualInterestRate) {
        log.info("Ejecutando evaluación automática para solicitud={} plazo={} meses", loanApplicationId, termInMonths);

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery(PROCEDURE_NAME);

            query.registerStoredProcedureParameter("p_application_id", UUID.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_decision", String.class, ParameterMode.INOUT);
            query.registerStoredProcedureParameter("p_max_capacity", BigDecimal.class, ParameterMode.INOUT);
            query.registerStoredProcedureParameter("p_current_debt", BigDecimal.class, ParameterMode.INOUT);
            query.registerStoredProcedureParameter("p_available_capacity", BigDecimal.class, ParameterMode.INOUT);
            query.registerStoredProcedureParameter("p_new_installment", BigDecimal.class, ParameterMode.INOUT);
            query.registerStoredProcedureParameter("p_debt_ratio", BigDecimal.class, ParameterMode.INOUT);
            query.registerStoredProcedureParameter("p_reason", String.class, ParameterMode.INOUT);

            query.setParameter("p_application_id", loanApplicationId);
            query.setParameter("p_decision", "");
            query.setParameter("p_max_capacity", BigDecimal.ZERO);
            query.setParameter("p_current_debt", BigDecimal.ZERO);
            query.setParameter("p_available_capacity", BigDecimal.ZERO);
            query.setParameter("p_new_installment", BigDecimal.ZERO);
            query.setParameter("p_debt_ratio", BigDecimal.ZERO);
            query.setParameter("p_reason", "");

            query.execute();

            String decision = (String) query.getOutputParameterValue("p_decision");
            BigDecimal maxCapacity = (BigDecimal) query.getOutputParameterValue("p_max_capacity");
            BigDecimal currentDebt = (BigDecimal) query.getOutputParameterValue("p_current_debt");
            BigDecimal availableCapacity = (BigDecimal) query.getOutputParameterValue("p_available_capacity");
            BigDecimal newInstallment = (BigDecimal) query.getOutputParameterValue("p_new_installment");
            BigDecimal debtRatio = (BigDecimal) query.getOutputParameterValue("p_debt_ratio");
            String reason = (String) query.getOutputParameterValue("p_reason");

            log.info("Evaluación automática completada para solicitud={}: decision={} debtRatio={}",
                loanApplicationId, decision, debtRatio);

            return new EvaluationResult(
                decision, maxCapacity, currentDebt,
                availableCapacity, newInstallment, debtRatio, reason);

        } catch (Exception e) {
            log.error("Error al ejecutar evaluación automática para solicitud={}", loanApplicationId, e);
            throw new RuntimeException(
                "Error al ejecutar la evaluación automática: " + e.getMessage(), e);
        }
    }
}
