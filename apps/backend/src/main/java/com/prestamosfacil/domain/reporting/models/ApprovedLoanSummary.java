package com.prestamosfacil.domain.reporting.models;

import java.math.BigDecimal;

public record ApprovedLoanSummary(
    BigDecimal totalApproved,
    long activeLoans,
    BigDecimal averageRate
) {}
