package com.prestamosfacil.domain.auth.port.out;

import java.util.UUID;

public interface TokenGeneratorPort {

    String createAccessToken(UUID userId, UUID sessionId, String email, String role);

    String createRefreshToken(UUID userId, UUID sessionId, UUID refreshJti, String role, String email);

    String generateRandomToken();
}
