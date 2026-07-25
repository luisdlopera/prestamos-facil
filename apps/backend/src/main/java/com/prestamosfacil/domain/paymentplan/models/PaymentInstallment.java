package com.prestamosfacil.domain.paymentplan.models;

import com.prestamosfacil.domain.shared.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaymentInstallment {

    @Builder.Default
    private final UUID id = UUID.randomUUID();
    private final UUID loanId;
    private final int installmentNumber;
    private final LocalDate dueDate;
    private final Money openingBalance;
    private final Money paymentAmount;
    private final Money principalAmount;
    private final Money interestAmount;
    private final Money closingBalance;

    public PaymentInstallment(UUID loanId, int installmentNumber, LocalDate dueDate, Money openingBalance,
                              Money paymentAmount, Money principalAmount, Money interestAmount, Money closingBalance) {
        this(UUID.randomUUID(), loanId, installmentNumber, dueDate, openingBalance,
             paymentAmount, principalAmount, interestAmount, closingBalance);
    }
}
