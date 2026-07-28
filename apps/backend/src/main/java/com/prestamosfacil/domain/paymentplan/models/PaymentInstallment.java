package com.prestamosfacil.domain.paymentplan.models;

import com.prestamosfacil.domain.shared.Money;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
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

    public PaymentInstallment(UUID id, UUID loanId, int installmentNumber, LocalDate dueDate,
                              Money openingBalance, Money paymentAmount, Money principalAmount,
                              Money interestAmount, Money closingBalance) {
        this.id = id != null ? id : UUID.randomUUID();
        this.loanId = loanId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.openingBalance = openingBalance;
        this.paymentAmount = paymentAmount;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.closingBalance = closingBalance;
    }

    public PaymentInstallment(UUID loanId, int installmentNumber, LocalDate dueDate, Money openingBalance,
                              Money paymentAmount, Money principalAmount, Money interestAmount, Money closingBalance) {
        this(UUID.randomUUID(), loanId, installmentNumber, dueDate, openingBalance,
             paymentAmount, principalAmount, interestAmount, closingBalance);
    }
}
