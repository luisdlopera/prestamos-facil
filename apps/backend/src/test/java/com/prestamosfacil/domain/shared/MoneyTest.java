package com.prestamosfacil.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithDefaultCurrency() {
        Money money = new Money(new BigDecimal("1000.00"));
        assertEquals(Currency.getInstance("COP"), money.getCurrency());
    }

    @Test
    void shouldThrowOnNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> new Money(null));
    }

    @Test
    void shouldThrowOnNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> new Money(new BigDecimal("-100")));
    }

    @Test
    void shouldAddMoney() {
        Money a = new Money(new BigDecimal("1000"));
        Money b = new Money(new BigDecimal("500"));
        Money result = a.add(b);
        assertEquals(new BigDecimal("1500.00"), result.getAmount());
    }

    @Test
    void shouldThrowOnCurrencyMismatch() {
        Money cop = new Money(new BigDecimal("1000"));
        Money usd = new Money(new BigDecimal("100"), Currency.getInstance("USD"));
        assertThrows(IllegalArgumentException.class, () -> cop.add(usd));
    }

    @Test
    void shouldCompareMoney() {
        Money a = new Money(new BigDecimal("1000"));
        Money b = new Money(new BigDecimal("2000"));
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }
}
