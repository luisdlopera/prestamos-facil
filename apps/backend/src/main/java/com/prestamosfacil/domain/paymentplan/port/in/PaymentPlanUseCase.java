package com.prestamosfacil.domain.paymentplan.port.in;

import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;

import java.util.List;
import java.util.UUID;

public interface PaymentPlanUseCase {

    List<PaymentInstallment> generatePaymentPlan(UUID loanId);

    List<PaymentInstallment> getPaymentPlan(UUID loanId);
}
