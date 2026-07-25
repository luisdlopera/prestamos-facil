package com.prestamosfacil.domain.notification.models;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class Notification {

    Customer customer;
    String applicationId;
    String loanTypeName;
    BigDecimal requestedAmount;
    int termInMonths;
    String reason;
    List<PaymentInstallment> installments;
}
