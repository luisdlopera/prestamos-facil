package com.prestamosfacil.domain.loantype.models;

import com.prestamosfacil.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoanTypeTest {

    @Test
    void shouldCreateActiveLoanTypeWithDefaults() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));

        assertEquals("Personal", loanType.getName());
        assertEquals(0, new BigDecimal("12.0").compareTo(loanType.getInterestRate()));
        assertTrue(loanType.isActive());
        assertFalse(loanType.isAutomaticValidationEnabled());
        assertEquals(1, loanType.getMinTermMonths());
        assertEquals(60, loanType.getMaxTermMonths());
    }

    @Test
    void shouldToggleAutomaticValidation() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setAutomaticValidationEnabled(true);
        assertTrue(loanType.isAutomaticValidationEnabled());
    }

    @Test
    void shouldToggleActive() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setActive(false);
        assertFalse(loanType.isActive());
    }

    @Test
    void shouldSetMinAndMaxTerm() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setMinTermMonths(3);
        loanType.setMaxTermMonths(24);
        assertEquals(3, loanType.getMinTermMonths());
        assertEquals(24, loanType.getMaxTermMonths());
    }

    @Test
    void shouldGetAmounts() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        assertEquals(0, new BigDecimal("1000000").compareTo(loanType.getMinAmount().getAmount()));
        assertEquals(0, new BigDecimal("50000000").compareTo(loanType.getMaxAmount().getAmount()));
    }

    @Test
    void shouldPassValidationForValidApplicationRequest() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setMinTermMonths(3);
        loanType.setMaxTermMonths(24);

        assertDoesNotThrow(() -> loanType.validateApplicationRequest(
            new Money(new BigDecimal("5000000")), 12));
    }

    @Test
    void shouldThrowWhenLoanTypeIsInactive() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setActive(false);

        assertThrows(IllegalArgumentException.class, () ->
            loanType.validateApplicationRequest(new Money(new BigDecimal("5000000")), 12));
    }

    @Test
    void shouldThrowWhenAmountBelowMinimum() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));

        assertThrows(IllegalArgumentException.class, () ->
            loanType.validateApplicationRequest(new Money(new BigDecimal("500000")), 12));
    }

    @Test
    void shouldThrowWhenAmountExceedsMaximum() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));

        assertThrows(IllegalArgumentException.class, () ->
            loanType.validateApplicationRequest(new Money(new BigDecimal("60000000")), 12));
    }

    @Test
    void shouldThrowWhenTermBelowMinimum() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setMinTermMonths(6);

        assertThrows(IllegalArgumentException.class, () ->
            loanType.validateApplicationRequest(new Money(new BigDecimal("5000000")), 3));
    }

    @Test
    void shouldThrowWhenTermExceedsMaximum() {
        LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        loanType.setMaxTermMonths(12);

        assertThrows(IllegalArgumentException.class, () ->
            loanType.validateApplicationRequest(new Money(new BigDecimal("5000000")), 24));
    }
}
