package com.prestamosfacil.domain.reporting.models;

import java.math.BigDecimal;
import java.time.Instant;

public record ApprovedLoansTotal(
    BigDecimal totalApprovedAmount,
    long approvedLoansCount,
    Instant generatedAt
) {}
