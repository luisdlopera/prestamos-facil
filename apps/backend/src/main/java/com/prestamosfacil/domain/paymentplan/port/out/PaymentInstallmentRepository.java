package com.prestamosfacil.domain.paymentplan.port.out;

import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;

import java.util.List;
import java.util.UUID;

public interface PaymentInstallmentRepository {

    List<PaymentInstallment> findByLoanId(UUID loanId);

    PaymentInstallment save(PaymentInstallment installment);

    List<PaymentInstallment> saveAll(List<PaymentInstallment> installments);
}
