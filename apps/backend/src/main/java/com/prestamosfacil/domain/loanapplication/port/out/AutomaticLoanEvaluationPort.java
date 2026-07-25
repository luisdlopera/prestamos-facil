package com.prestamosfacil.domain.loanapplication.port.out;

import com.prestamosfacil.domain.loanapplication.models.EvaluationResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface AutomaticLoanEvaluationPort {

    EvaluationResult evaluate(UUID loanApplicationId, BigDecimal baseSalary,
                              BigDecimal requestedAmount, int termInMonths,
                              BigDecimal annualInterestRate);
}
