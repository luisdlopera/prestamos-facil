package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment_installments",
    uniqueConstraints = @UniqueConstraint(name = "uq_installments_loan_number", columnNames = {"loan_id", "installment_number"}))
@Check(constraints = "opening_balance >= 0 AND payment_amount >= 0 AND principal_amount >= 0 "
    + "AND interest_amount >= 0 AND closing_balance >= 0")
public class PaymentInstallmentEntity {
    @Id private UUID id;
    @Column(name = "loan_id", nullable = false) private UUID loanId;
    @Column(name = "installment_number", nullable = false) private int installmentNumber;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(name = "opening_balance", nullable = false, precision = 15, scale = 2) private BigDecimal openingBalance;
    @Column(name = "payment_amount", nullable = false, precision = 15, scale = 2) private BigDecimal paymentAmount;
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2) private BigDecimal principalAmount;
    @Column(name = "interest_amount", nullable = false, precision = 15, scale = 2) private BigDecimal interestAmount;
    @Column(name = "closing_balance", nullable = false, precision = 15, scale = 2) private BigDecimal closingBalance;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public PaymentInstallmentEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID v) { this.loanId = v; }
    public int getInstallmentNumber() { return installmentNumber; }
    public void setInstallmentNumber(int v) { this.installmentNumber = v; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate v) { this.dueDate = v; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal v) { this.openingBalance = v; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal v) { this.paymentAmount = v; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal v) { this.principalAmount = v; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal v) { this.interestAmount = v; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal v) { this.closingBalance = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
