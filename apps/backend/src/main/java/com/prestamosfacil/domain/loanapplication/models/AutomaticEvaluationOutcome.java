package com.prestamosfacil.domain.loanapplication.models;

import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;

import java.math.BigDecimal;

public record AutomaticEvaluationOutcome(
        LoanApplicationStatus status,
        BigDecimal maxCapacity,
        BigDecimal currentDebt,
        BigDecimal newInstallment,
        BigDecimal debtRatio,
        String reason) {
}
