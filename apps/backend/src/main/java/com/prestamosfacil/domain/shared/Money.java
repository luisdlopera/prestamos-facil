package com.prestamosfacil.domain.shared;

import com.prestamosfacil.domain.shared.constants.MoneyConstants;
import com.prestamosfacil.domain.shared.enums.Messages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money implements ValueObject {

    private final BigDecimal amount;
    private final Currency currency;

    private static final Currency DEFAULT_CURRENCY = Currency.getInstance(MoneyConstants.DEFAULT_CURRENCY);
    private static final int DECIMAL_PLACES = MoneyConstants.DECIMAL_PLACES;

    public Money(BigDecimal amount) {
        this(amount, DEFAULT_CURRENCY);
    }

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException(Messages.MONEY_AMOUNT_REQUIRED.getValue());
        }
        if (currency == null) {
            throw new IllegalArgumentException(Messages.MONEY_CURRENCY_REQUIRED.getValue());
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(Messages.MONEY_AMOUNT_NEGATIVE.getValue());
        }
        this.amount = amount.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public int compareTo(Money other) {
        validateSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                Messages.MONEY_CURRENCY_MISMATCH.format(this.currency, other.currency)
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return String.format("%s %,." + DECIMAL_PLACES + "f", currency.getSymbol(), amount);
    }
}
