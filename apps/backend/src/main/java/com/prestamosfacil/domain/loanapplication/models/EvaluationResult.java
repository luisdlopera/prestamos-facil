package com.prestamosfacil.domain.loanapplication.models;

import java.math.BigDecimal;

public record EvaluationResult(
    String decision,
    BigDecimal maxCapacity,
    BigDecimal currentDebt,
    BigDecimal availableCapacity,
    BigDecimal newInstallment,
    BigDecimal debtRatio,
    String reason) {}
