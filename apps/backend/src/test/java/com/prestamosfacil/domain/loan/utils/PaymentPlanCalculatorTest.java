package com.prestamosfacil.domain.loan.utils;

import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentPlanCalculatorTest {

    private PaymentPlanCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PaymentPlanCalculator();
    }

    @Test
    void shouldCalculateInstallments() {
        UUID loanId = UUID.randomUUID();
        BigDecimal principal = new BigDecimal("1000000");
        BigDecimal annualInterestRate = new BigDecimal("24.0");
        int termInMonths = 12;
        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        List<PaymentInstallment> installments = calculator.calculateInstallments(
                loanId, principal, annualInterestRate, termInMonths, firstDueDate);

        assertNotNull(installments);
        assertEquals(12, installments.size());
        assertEquals(1, installments.get(0).getInstallmentNumber());
        assertEquals(12, installments.get(11).getInstallmentNumber());
        assertEquals(BigDecimal.ZERO.setScale(2), installments.get(11).getClosingBalance().getAmount());
    }

    @Test
    void shouldUseAnnualPercentageDividedByTwelveAndCloseTheFinalBalance() {
        BigDecimal monthlyRate = calculator.calculateMonthlyRate(new BigDecimal("18.5"));
        assertEquals(new BigDecimal("0.0154166666666667"), monthlyRate);

        BigDecimal payment = calculator.calculatePayment(
            new BigDecimal("1000000"), monthlyRate, 24);
        assertTrue(payment.subtract(new BigDecimal("50166.02536057134"))
            .abs().compareTo(new BigDecimal("0.000000001")) < 0);

        List<PaymentInstallment> installments = calculator.calculateInstallments(
            UUID.randomUUID(), new BigDecimal("1000000"), new BigDecimal("18.5"),
            24, LocalDate.of(2026, 8, 27));

        assertEquals(24, installments.size());
        assertEquals(LocalDate.of(2026, 8, 27), installments.get(0).getDueDate());
        assertEquals(new BigDecimal("15416.67"), installments.get(0).getInterestAmount().getAmount());
        assertEquals(new BigDecimal("34749.36"), installments.get(0).getPrincipalAmount().getAmount());
        assertEquals(new BigDecimal("965250.64"), installments.get(0).getClosingBalance().getAmount());
        assertEquals(new BigDecimal("0.00"), installments.get(23).getClosingBalance().getAmount());
    }

    @Test
    void shouldCalculateMonthlyRateZeroWhenNullOrZero() {
        assertEquals(BigDecimal.ZERO, calculator.calculateMonthlyRate(null));
        assertEquals(BigDecimal.ZERO, calculator.calculateMonthlyRate(BigDecimal.ZERO));
    }

    @Test
    void shouldThrowOnInvalidPrincipal() {
        assertThrows(IllegalArgumentException.class, () ->
            calculator.calculateInstallments(UUID.randomUUID(), BigDecimal.ZERO, new BigDecimal("24.0"), 12, LocalDate.now()));
    }

    @Test
    void shouldThrowOnNegativeInterestRate() {
        assertThrows(IllegalArgumentException.class, () ->
            calculator.calculateInstallments(UUID.randomUUID(), new BigDecimal("1000"), new BigDecimal("-1.0"), 12, LocalDate.now()));
    }

    @Test
    void shouldThrowOnInvalidTerm() {
        assertThrows(IllegalArgumentException.class, () ->
            calculator.calculateInstallments(UUID.randomUUID(), new BigDecimal("1000"), new BigDecimal("24.0"), 0, LocalDate.now()));
    }
}
