package com.prestamosfacil.domain.auth.port.out;

import com.prestamosfacil.domain.auth.models.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    PasswordResetToken save(PasswordResetToken token);
}
