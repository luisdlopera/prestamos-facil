package com.prestamosfacil.domain.paymentplan.models;

import com.prestamosfacil.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentInstallmentTest {

    @Test
    void shouldCreateInstallmentWithoutId() {
        UUID loanId = UUID.randomUUID();
        PaymentInstallment pi = new PaymentInstallment(
            loanId, 1, LocalDate.of(2026, 8, 1),
            new Money(new BigDecimal("10000000")),
            new Money(new BigDecimal("919181.18")),
            new Money(new BigDecimal("819181.18")),
            new Money(new BigDecimal("100000.00")),
            new Money(new BigDecimal("9180818.82"))
        );

        assertEquals(loanId, pi.getLoanId());
        assertEquals(1, pi.getInstallmentNumber());
        assertEquals(LocalDate.of(2026, 8, 1), pi.getDueDate());
        assertNotNull(pi.getId());
    }

    @Test
    void shouldCreateInstallmentWithId() {
        UUID id = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();
        PaymentInstallment pi = new PaymentInstallment(
            id,
            loanId, 1, LocalDate.of(2026, 8, 1),
            new Money(new BigDecimal("10000000")),
            new Money(new BigDecimal("919181.18")),
            new Money(new BigDecimal("819181.18")),
            new Money(new BigDecimal("100000.00")),
            new Money(new BigDecimal("9180818.82"))
        );

        assertEquals(id, pi.getId());
        assertEquals(loanId, pi.getLoanId());
    }

    @Test
    void shouldReturnAllMoneyFields() {
        Money opening = new Money(new BigDecimal("10000000"));
        Money payment = new Money(new BigDecimal("919181.18"));
        Money principal = new Money(new BigDecimal("819181.18"));
        Money interest = new Money(new BigDecimal("100000.00"));
        Money closing = new Money(new BigDecimal("9180818.82"));

        PaymentInstallment pi = new PaymentInstallment(
            UUID.randomUUID(), 1, LocalDate.of(2026, 8, 1),
            opening, payment, principal, interest, closing
        );

        assertEquals(opening, pi.getOpeningBalance());
        assertEquals(payment, pi.getPaymentAmount());
        assertEquals(principal, pi.getPrincipalAmount());
        assertEquals(interest, pi.getInterestAmount());
        assertEquals(closing, pi.getClosingBalance());
    }
}
