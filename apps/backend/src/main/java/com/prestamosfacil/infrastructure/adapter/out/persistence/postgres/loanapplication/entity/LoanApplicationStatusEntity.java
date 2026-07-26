package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "loan_application_statuses")
public class LoanApplicationStatusEntity {
    @Id @Column(length = 20) private String code;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 255) private String description;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public LoanApplicationStatusEntity() {}
    public LoanApplicationStatusEntity(String code) { this.code = code; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public static LoanApplicationStatusEntity fromCode(String code) { return new LoanApplicationStatusEntity(code); }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
