package com.prestamosfacil.domain.loan.utils;

import com.prestamosfacil.domain.loan.constants.LoanConstants;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.enums.Messages;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentPlanCalculator {

    private static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);
    private static final int MONTHLY_RATE_SCALE = 16;

    public List<PaymentInstallment> calculateInstallments(
            UUID loanId, BigDecimal principal, BigDecimal annualInterestRate,
            int termInMonths, LocalDate firstDueDate) {

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(Messages.LOAN_PRINCIPAL_REQUIRED.getValue());
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(Messages.LOAN_RATE_NON_NEGATIVE.getValue());
        }
        if (termInMonths <= 0) {
            throw new IllegalArgumentException(Messages.LOAN_TERM_REQUIRED.getValue());
        }

        BigDecimal monthlyRate = calculateMonthlyRate(annualInterestRate);

        // Mantener la cuota sin redondear durante toda la amortización. Money
        // aplica la escala monetaria únicamente al construir cada fila.
        BigDecimal payment = calculatePayment(principal, monthlyRate, termInMonths);
        List<PaymentInstallment> installments = new ArrayList<>(termInMonths);
        BigDecimal openingBalance = principal;

        for (int i = 1; i <= termInMonths; i++) {
            BigDecimal interest = openingBalance.multiply(monthlyRate, MC);
            BigDecimal principalPaid = payment.subtract(interest, MC);
            BigDecimal closingBalance = openingBalance.subtract(principalPaid, MC);

            if (i == termInMonths) {
                principalPaid = openingBalance;
                payment = principalPaid.add(interest, MC);
                closingBalance = BigDecimal.ZERO.setScale(2);
            }

            installments.add(PaymentInstallment.builder()
                .id(UUID.randomUUID())
                .loanId(loanId)
                .installmentNumber(i)
                .dueDate(firstDueDate.plusMonths(i - 1))
                .openingBalance(new Money(openingBalance))
                .paymentAmount(new Money(payment))
                .principalAmount(new Money(principalPaid))
                .interestAmount(new Money(interest))
                .closingBalance(new Money(closingBalance))
                .build());

            openingBalance = closingBalance;
        }

        return installments;
    }

    public BigDecimal calculatePayment(BigDecimal principal, BigDecimal monthlyRate, int term) {
        if (monthlyRate == null || monthlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(Messages.LOAN_RATE_NON_NEGATIVE.getValue());
        }
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(term), MC);
        }
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal factor = onePlusR.pow(term, MC);
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(factor, MC);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE, MC);
        return numerator.divide(denominator, MC);
    }

    /**
     * Tasa mensual simple: i = (tasa anual / 100) / 12. La tasa anual llega expresada
     * como porcentaje (ej. 18.5 = 18.5%), no como decimal.
     */
    public BigDecimal calculateMonthlyRate(BigDecimal annualInterestRate) {
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return annualInterestRate
            .divide(BigDecimal.valueOf(LoanConstants.PERCENTAGE_DIVISOR), MONTHLY_RATE_SCALE, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(LoanConstants.MONTHS_PER_YEAR), MONTHLY_RATE_SCALE, RoundingMode.HALF_UP);
    }
}
