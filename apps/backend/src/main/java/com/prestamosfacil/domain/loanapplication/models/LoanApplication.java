package com.prestamosfacil.domain.loanapplication.models;

import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.loanapplication.exceptions.InvalidLoanApplicationStateException;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loantype.models.LoanType;
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

import static java.util.Objects.requireNonNull;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private Customer customer;
    private LoanType loanType;
    private Money requestedAmount;
    private int termInMonths;
    private BigDecimal annualInterestRate;
    private LoanApplicationStatusEntry currentStatusEntry;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public LoanApplication(Customer customer, LoanType loanType, Money requestedAmount, int termInMonths) {
        this.id = UUID.randomUUID();
        this.customer = requireNonNull(customer);
        this.loanType = requireNonNull(loanType);
        this.requestedAmount = requireNonNull(requestedAmount);
        this.termInMonths = termInMonths;
        this.annualInterestRate = loanType.getInterestRate();
        this.currentStatusEntry = LoanApplicationStatusEntry.initialEntry(this.id);
        this.createdAt = Instant.now();
        validate();
    }

    public void validate() {
        if (customer == null) {
            throw new IllegalArgumentException(Messages.LOAN_APPLICATION_CUSTOMER_REQUIRED.getValue());
        }
        if (loanType == null) {
            throw new IllegalArgumentException(Messages.LOAN_APPLICATION_LOAN_TYPE_REQUIRED.getValue());
        }
        if (requestedAmount == null || requestedAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(Messages.LOAN_APPLICATION_AMOUNT_REQUIRED.getValue());
        }
        if (termInMonths < 1) {
            throw new IllegalArgumentException(Messages.LOAN_APPLICATION_MIN_TERM.getValue());
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(Messages.LOAN_RATE_NON_NEGATIVE.getValue());
        }
    }

    /** Retorna el estado actual de la solicitud (delegado al historial activo). */
    public LoanApplicationStatus getStatus() {
        return currentStatusEntry != null
            ? currentStatusEntry.getStatus()
            : LoanApplicationStatus.PENDING_REVIEW;
    }

    /** Razón de la última decisión tomada sobre la solicitud. */
    public String getDecisionReason() {
        return currentStatusEntry != null ? currentStatusEntry.getDecisionReason() : null;
    }

    /** Instante en que el estado actual se abrió. */
    public Instant getEvaluatedAt() {
        return currentStatusEntry != null ? currentStatusEntry.getOpenedAt() : null;
    }

    /** UUID del staff que tomó la última decisión. */
    public UUID getEvaluatedBy() {
        return currentStatusEntry != null ? currentStatusEntry.getEvaluatedBy() : null;
    }

    public boolean canBeDecided() {
        LoanApplicationStatus s = getStatus();
        return s == LoanApplicationStatus.PENDING_REVIEW
            || s == LoanApplicationStatus.MANUAL_REVIEW;
    }

    public LoanApplication approve() {
        return approve(null);
    }

    public LoanApplication approve(UUID staffId) {
        if (!canBeDecided()) {
            throw new InvalidLoanApplicationStateException(
                Messages.LOAN_APPLICATION_CANNOT_APPROVE.format(getStatus()));
        }
        LoanApplicationStatusEntry newEntry = LoanApplicationStatusEntry.transitionTo(
            id, LoanApplicationStatus.APPROVED, getDecisionReason(), staffId);
        return buildWithNewEntry(newEntry);
    }

    public LoanApplication reject(String reason) {
        return reject(reason, null);
    }

    public LoanApplication reject(String reason, UUID staffId) {
        if (!canBeDecided()) {
            throw new InvalidLoanApplicationStateException(
                Messages.LOAN_APPLICATION_CANNOT_REJECT.format(getStatus()));
        }
        LoanApplicationStatusEntry newEntry = LoanApplicationStatusEntry.transitionTo(
            id, LoanApplicationStatus.REJECTED, reason, staffId);
        return buildWithNewEntry(newEntry);
    }

    public LoanApplication autoEvaluate(String decision, String reason) {
        LoanApplicationStatus newStatus = switch (decision) {
            case "APPROVED"      -> LoanApplicationStatus.APPROVED;
            case "MANUAL_REVIEW" -> LoanApplicationStatus.MANUAL_REVIEW;
            default              -> LoanApplicationStatus.REJECTED;
        };
        LoanApplicationStatusEntry newEntry = LoanApplicationStatusEntry.transitionTo(
            id, newStatus, reason, null);
        return buildWithNewEntry(newEntry);
    }

    public LoanApplication withDecisionReason(String reason) {
        if (currentStatusEntry == null) return this;
        LoanApplicationStatusEntry updated = LoanApplicationStatusEntry.builder()
            .id(currentStatusEntry.getId())
            .loanApplicationId(currentStatusEntry.getLoanApplicationId())
            .status(currentStatusEntry.getStatus())
            .decisionReason(reason)
            .evaluatedBy(currentStatusEntry.getEvaluatedBy())
            .openedAt(currentStatusEntry.getOpenedAt())
            .closedAt(currentStatusEntry.getClosedAt())
            .build();
        return buildWithNewEntry(updated);
    }

    private LoanApplication buildWithNewEntry(LoanApplicationStatusEntry newEntry) {
        return LoanApplication.builder()
            .id(this.id)
            .customer(this.customer)
            .loanType(this.loanType)
            .requestedAmount(this.requestedAmount)
            .termInMonths(this.termInMonths)
            .annualInterestRate(this.annualInterestRate)
            .currentStatusEntry(newEntry)
            .build();
    }
}
