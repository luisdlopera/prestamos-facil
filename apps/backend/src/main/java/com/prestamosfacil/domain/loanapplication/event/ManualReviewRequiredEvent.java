package com.prestamosfacil.domain.loanapplication.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ManualReviewRequiredEvent(
    UUID applicationId,
    UUID customerId,
    String customerName,
    String customerEmail,
    String loanTypeName,
    BigDecimal requestedAmount,
    int termInMonths
) {}
