package com.prestamosfacil.domain.auth.port.out;

import com.prestamosfacil.domain.auth.models.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findById(UUID id);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    RefreshToken save(RefreshToken refreshToken);
    void revokeByTokenHash(String tokenHash);
    void revokeAllForUser(UUID userId);
}
