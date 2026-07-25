package com.prestamosfacil.domain.loanapplication.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplicationReceivedEvent(
    UUID applicationId,
    UUID customerId,
    String customerName,
    String customerEmail,
    String loanTypeName,
    BigDecimal requestedAmount,
    int termInMonths
) {}
