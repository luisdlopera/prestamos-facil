package com.prestamosfacil.domain.auth.models;

import java.util.UUID;

public record LoginResult(
    UUID aggregateId,
    String role,
    String accessToken,
    String refreshToken,
    long expiresIn
) {}
