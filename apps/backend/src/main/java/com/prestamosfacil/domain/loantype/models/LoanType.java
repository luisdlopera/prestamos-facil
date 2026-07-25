package com.prestamosfacil.domain.loantype.models;

import com.prestamosfacil.domain.loantype.enums.RateType;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.enums.Messages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanType {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal interestRate;
    private RateType rateType;
    private boolean automaticValidationEnabled;
    private Money minAmount;
    private Money maxAmount;
    private boolean active;
    private int minTermMonths;
    private int maxTermMonths;
    private int displayOrder;
    private Instant createdAt;
    private Instant updatedAt;

    public LoanType(String name, BigDecimal interestRate, Money minAmount, Money maxAmount) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = "";
        this.interestRate = interestRate;
        this.rateType = RateType.EA;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.automaticValidationEnabled = false;
        this.active = true;
        this.minTermMonths = 1;
        this.maxTermMonths = 60;
        this.displayOrder = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_NAME_REQUIRED.getValue());
        }
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0 || interestRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_INTEREST_RATE_INVALID.getValue());
        }
        if (minAmount == null || minAmount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_MIN_AMOUNT_REQUIRED.getValue());
        }
        if (maxAmount == null || maxAmount.getAmount().compareTo(minAmount.getAmount()) < 0) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_MAX_AMOUNT_INVALID.getValue());
        }
        if (minTermMonths < 1) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_MIN_TERM_REQUIRED.getValue());
        }
        if (maxTermMonths < minTermMonths) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_MAX_TERM_INVALID.getValue());
        }
    }

    public void validateApplicationRequest(Money requestedAmount, int termInMonths) {
        if (!active) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_NOT_ACTIVE.getValue());
        }
        if (requestedAmount.compareTo(minAmount) < 0) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_AMOUNT_BELOW_MINIMUM.getValue());
        }
        if (requestedAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(Messages.LOAN_TYPE_AMOUNT_EXCEEDS_MAXIMUM.getValue());
        }
        if (termInMonths < minTermMonths) {
            throw new IllegalArgumentException(
                Messages.LOAN_TYPE_TERM_BELOW_MINIMUM.format(minTermMonths));
        }
        if (termInMonths > maxTermMonths) {
            throw new IllegalArgumentException(
                Messages.LOAN_TYPE_TERM_EXCEEDS_MAXIMUM.format(maxTermMonths));
        }
    }
}
