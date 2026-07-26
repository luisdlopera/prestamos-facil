package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_types")
public class DocumentTypeEntity {
    @Id @Column(length = 10) private String code;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 255) private String description;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public DocumentTypeEntity() {}
    public DocumentTypeEntity(String code) { this.code = code; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
