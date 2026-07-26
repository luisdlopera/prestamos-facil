package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Check(constraints = "requested_amount > 0 AND term_in_months > 0")
public class LoanApplicationEntity {
    @Id private UUID id;
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "loan_type_id", nullable = false) private UUID loanTypeId;
    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2) private BigDecimal requestedAmount;
    @Column(name = "term_in_months", nullable = false) private int termInMonths;
    @Column(name = "annual_interest_rate", nullable = false, precision = 5, scale = 2) private BigDecimal annualInterestRate;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(name = "version", nullable = false) private Long version;

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public LoanApplicationEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID v) { this.customerId = v; }
    public UUID getLoanTypeId() { return loanTypeId; }
    public void setLoanTypeId(UUID v) { this.loanTypeId = v; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal v) { this.requestedAmount = v; }
    public int getTermInMonths() { return termInMonths; }
    public void setTermInMonths(int v) { this.termInMonths = v; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal v) { this.annualInterestRate = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }
}
