package com.prestamosfacil.domain.auth.models;

import com.prestamosfacil.domain.user.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PasswordResetToken {

    @Builder.Default
    private final UUID id = UUID.randomUUID();
    private final User user;
    private final String tokenHash;
    private final Instant expiresAt;
    private final boolean used;

    public PasswordResetToken(User user, String tokenHash, Instant expiresAt) {
        this(UUID.randomUUID(), user, tokenHash, expiresAt, false);
    }

    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    public PasswordResetToken markUsed() {
        return PasswordResetToken.builder()
                .id(id)
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .used(true)
                .build();
    }
}
