package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Check(constraints = "length(btrim(first_name)) > 0 AND length(btrim(last_name)) > 0 "
    + "AND base_salary >= 0 AND base_salary <= 15000000.00")
public class CustomerEntity {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "document_type", nullable = false)
    private DocumentTypeEntity documentType;
    @Column(name = "document_number", nullable = false, unique = true, length = 50) private String documentNumber;
    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2) private BigDecimal baseSalary;
    @Column(length = 30) private String phone;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public CustomerEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { firstName = value; }
    public String getLastName() { return lastName; }
    public void setLastName(String value) { lastName = value; }
    public DocumentTypeEntity getDocumentType() { return documentType; }
    public void setDocumentType(DocumentTypeEntity value) { documentType = value; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String value) { documentNumber = value; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal value) { baseSalary = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
