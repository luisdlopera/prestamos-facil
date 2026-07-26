package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Check(constraints = "principal_amount > 0 AND annual_interest_rate >= 0 AND annual_interest_rate <= 100 "
    + "AND term_in_months > 0 AND monthly_payment >= 0 "
    + "AND status IN ('APPROVED','PAID','CANCELLED','DEFAULTED')")
public class LoanEntity {
    @Id private UUID id;
    @Column(name = "loan_application_id", nullable = false, unique = true) private UUID loanApplicationId;
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2) private BigDecimal principalAmount;
    @Column(name = "annual_interest_rate", nullable = false, precision = 5, scale = 2) private BigDecimal annualInterestRate;
    @Column(name = "term_in_months", nullable = false) private int termInMonths;
    @Column(name = "monthly_payment", nullable = false, precision = 15, scale = 2) private BigDecimal monthlyPayment;
    @Column(name = "status", nullable = false, length = 20) private String status = "APPROVED";
    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(name = "version", nullable = false) private Long version;

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public LoanEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getLoanApplicationId() { return loanApplicationId; }
    public void setLoanApplicationId(UUID v) { this.loanApplicationId = v; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID v) { this.customerId = v; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal v) { this.principalAmount = v; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal v) { this.annualInterestRate = v; }
    public int getTermInMonths() { return termInMonths; }
    public void setTermInMonths(int v) { this.termInMonths = v; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal v) { this.monthlyPayment = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant v) { this.approvedAt = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }
}
