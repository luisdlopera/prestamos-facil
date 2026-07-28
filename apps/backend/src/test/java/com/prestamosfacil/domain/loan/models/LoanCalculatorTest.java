package com.prestamosfacil.domain.loan.models;

import com.prestamosfacil.domain.loan.utils.PaymentPlanCalculator;
import com.prestamosfacil.domain.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class LoanCalculatorTest {

    private LoanCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new LoanCalculator(new PaymentPlanCalculator());
    }

    @Test
    void shouldCalculateMonthlyPaymentWithPositiveRate() {
        Money result = calculator.calculateMonthlyPayment(
            new BigDecimal("10000000"), new BigDecimal("18.50"), 12);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("919181.18"));
    }

    @Test
    void shouldCalculateMonthlyPaymentWithZeroRate() {
        Money result = calculator.calculateMonthlyPayment(
            new BigDecimal("12000000"), BigDecimal.ZERO, 12);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000000.00"));
    }

    @Test
    void shouldThrowOnNegativePrincipal() {
        assertThatThrownBy(() -> calculator.calculateMonthlyPayment(
            new BigDecimal("-1000"), new BigDecimal("10"), 12))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnZeroPrincipal() {
        assertThatThrownBy(() -> calculator.calculateMonthlyPayment(
            BigDecimal.ZERO, new BigDecimal("10"), 12))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNegativeRate() {
        assertThatThrownBy(() -> calculator.calculateMonthlyPayment(
            new BigDecimal("10000"), new BigDecimal("-5"), 12))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnZeroTerm() {
        assertThatThrownBy(() -> calculator.calculateMonthlyPayment(
            new BigDecimal("10000"), new BigDecimal("10"), 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowOnNegativeTerm() {
        assertThatThrownBy(() -> calculator.calculateMonthlyPayment(
            new BigDecimal("10000"), new BigDecimal("10"), -1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHaveTwoDecimalPrecision() {
        Money result = calculator.calculateMonthlyPayment(
            new BigDecimal("9999999"), new BigDecimal("15.5"), 24);
        assertThat(result.getAmount().scale()).isEqualTo(2);
    }

    @Test
    void shouldCalculateTheTechnicalTestPayment() {
        Money result = calculator.calculateMonthlyPayment(
            new BigDecimal("1000000"), new BigDecimal("18.5"), 24);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50166.03"));
    }
}
