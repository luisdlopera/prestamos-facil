package com.prestamosfacil.domain.loan.models;

import com.prestamosfacil.domain.loan.utils.PaymentPlanCalculator;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.enums.Messages;
import java.math.BigDecimal;

public class LoanCalculator {

    private final PaymentPlanCalculator paymentPlanCalculator;

    public LoanCalculator(PaymentPlanCalculator paymentPlanCalculator) {
        this.paymentPlanCalculator = paymentPlanCalculator;
    }

    public Money calculateMonthlyPayment(BigDecimal principal, BigDecimal annualInterestRate, int termInMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(Messages.LOAN_PRINCIPAL_REQUIRED.getValue());
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(Messages.LOAN_RATE_NON_NEGATIVE.getValue());
        }
        if (termInMonths <= 0) {
            throw new IllegalArgumentException(Messages.LOAN_TERM_REQUIRED.getValue());
        }

        BigDecimal monthlyRate = paymentPlanCalculator.calculateMonthlyRate(annualInterestRate);

        return new Money(paymentPlanCalculator.calculatePayment(principal, monthlyRate, termInMonths));
    }
}
