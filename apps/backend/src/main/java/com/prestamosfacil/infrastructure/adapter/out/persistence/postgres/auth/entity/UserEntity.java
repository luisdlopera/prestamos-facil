package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false, length = 20) private String role = "CUSTOMER";
    @Column(name = "failed_login_attempts", nullable = false) private int failedLoginAttempts;
    @Column(name = "blocked_until") private Instant blockedUntil;
    @Column(name = "last_login_at") private Instant lastLoginAt;
    @Column(nullable = false) private boolean enabled = true;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist protected void onCreate() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = Instant.now(); }

    public UserEntity() {}
    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { passwordHash = value; }
    public String getRole() { return role; }
    public void setRole(String value) { role = value; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int value) { failedLoginAttempts = value; }
    public Instant getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(Instant value) { blockedUntil = value; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant value) { lastLoginAt = value; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
}
