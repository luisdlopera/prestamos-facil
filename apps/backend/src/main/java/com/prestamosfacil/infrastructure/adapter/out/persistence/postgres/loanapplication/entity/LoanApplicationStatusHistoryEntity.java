package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_application_status_history")
public class LoanApplicationStatusHistoryEntity {
    @Id private UUID id;
    @Column(name = "loan_application_id", nullable = false) private UUID loanApplicationId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "status", nullable = false) private LoanApplicationStatusEntity status;
    @Column(name = "decision_reason", length = 500) private String decisionReason;
    @Column(name = "evaluated_by") private UUID evaluatedBy;
    @Column(name = "opened_at", nullable = false) private Instant openedAt;
    @Column(name = "closed_at") private Instant closedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; if (openedAt == null) openedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public LoanApplicationStatusHistoryEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getLoanApplicationId() { return loanApplicationId; }
    public void setLoanApplicationId(UUID v) { this.loanApplicationId = v; }
    public LoanApplicationStatusEntity getStatus() { return status; }
    public void setStatus(LoanApplicationStatusEntity v) { this.status = v; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String v) { this.decisionReason = v; }
    public UUID getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(UUID v) { this.evaluatedBy = v; }
    public Instant getOpenedAt() { return openedAt; }
    public void setOpenedAt(Instant v) { this.openedAt = v; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant v) { this.closedAt = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
