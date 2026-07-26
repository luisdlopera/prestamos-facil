package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_types")
@Check(constraints = "annual_interest_rate >= 0 AND annual_interest_rate <= 100 "
    + "AND minimum_amount > 0 AND maximum_amount >= minimum_amount")
public class LoanTypeEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 100) private String name;
    @Column(length = 500) private String description;
    @Column(name = "annual_interest_rate", nullable = false, precision = 5, scale = 2) private BigDecimal annualInterestRate;
    @Column(name = "rate_type", nullable = false, length = 10) private String rateType = "EA";
    @Column(name = "automatic_validation_enabled", nullable = false) private boolean automaticValidationEnabled;
    @Column(name = "minimum_amount", nullable = false, precision = 15, scale = 2) private BigDecimal minimumAmount;
    @Column(name = "maximum_amount", nullable = false, precision = 15, scale = 2) private BigDecimal maximumAmount;
    @Column(name = "minimum_term_months", nullable = false) private int minimumTermMonths = 1;
    @Column(name = "maximum_term_months", nullable = false) private int maximumTermMonths = 60;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public LoanTypeEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal v) { this.annualInterestRate = v; }
    public String getRateType() { return rateType; }
    public void setRateType(String v) { this.rateType = v; }
    public boolean isAutomaticValidationEnabled() { return automaticValidationEnabled; }
    public void setAutomaticValidationEnabled(boolean v) { this.automaticValidationEnabled = v; }
    public BigDecimal getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(BigDecimal v) { this.minimumAmount = v; }
    public BigDecimal getMaximumAmount() { return maximumAmount; }
    public void setMaximumAmount(BigDecimal v) { this.maximumAmount = v; }
    public int getMinimumTermMonths() { return minimumTermMonths; }
    public void setMinimumTermMonths(int v) { this.minimumTermMonths = v; }
    public int getMaximumTermMonths() { return maximumTermMonths; }
    public void setMaximumTermMonths(int v) { this.maximumTermMonths = v; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int v) { this.displayOrder = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
