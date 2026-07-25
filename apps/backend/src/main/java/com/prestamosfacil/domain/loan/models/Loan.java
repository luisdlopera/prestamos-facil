package com.prestamosfacil.domain.loan.models;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loan.enums.LoanStatus;
import com.prestamosfacil.domain.shared.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private LoanApplication loanApplication;
    private Customer customer;
    private Money principalAmount;
    private BigDecimal annualInterestRate;
    private int termInMonths;
    private Money monthlyPayment;
    @Builder.Default
    private LoanStatus status = LoanStatus.APPROVED;
    private Instant approvedAt;

    public Loan(LoanApplication loanApplication, Customer customer,
                Money principalAmount, BigDecimal annualInterestRate,
                int termInMonths, Money monthlyPayment, BigDecimal monthlyInterestRate) {
        this(UUID.randomUUID(), loanApplication, customer, principalAmount, annualInterestRate,
             termInMonths, monthlyPayment, LoanStatus.APPROVED, Instant.now());
    }

    public LocalDate calculateFirstDueDate() {
        return approvedAt != null
            ? approvedAt.atZone(ZoneOffset.UTC).toLocalDate().plusMonths(1)
            : LocalDate.now(ZoneOffset.UTC).plusMonths(1);
    }
}
