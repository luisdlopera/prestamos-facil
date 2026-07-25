package com.prestamosfacil.domain.loanapplication.event;

import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ApplicationApprovedEvent(
    UUID applicationId,
    UUID customerId,
    String customerName,
    String customerEmail,
    String loanTypeName,
    BigDecimal requestedAmount,
    int termInMonths,
    List<PaymentInstallment> installments
) {}
