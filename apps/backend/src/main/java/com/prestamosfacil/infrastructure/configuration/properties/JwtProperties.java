package com.prestamosfacil.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record JwtProperties(
    String jwtSecret,
    long accessTokenTtlMinutes,
    long refreshTokenTtlDays,
    long clockSkewSeconds,
    Cookie cookie
) {
    public record Cookie(
        boolean secure,
        String sameSite,
        String accessName,
        String refreshName,
        String path,
        boolean httpOnly
    ) {}
}
