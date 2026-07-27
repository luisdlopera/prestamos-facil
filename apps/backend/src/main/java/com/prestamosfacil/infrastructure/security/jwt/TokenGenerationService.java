package com.prestamosfacil.infrastructure.security.jwt;

import com.prestamosfacil.domain.auth.port.out.TokenGeneratorPort;
import io.jsonwebtoken.Jwts;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TokenGenerationService implements TokenGeneratorPort {

    private final JwtTokenConfig config;

    public TokenGenerationService(JwtTokenConfig config) {
        this.config = config;
    }

    @Override
    public String createAccessToken(UUID userId, UUID sessionId, String email, String role) {
        OffsetDateTime now = OffsetDateTime.now();
        String type = "CUSTOMER".equals(role) ? "customer" : "staff";
        return Jwts.builder().subject(userId.toString())
                .issuedAt(Date.from(now.toInstant())).expiration(Date.from(now.plus(config.getAccessTtl()).toInstant()))
                .claim("sid", sessionId.toString()).claim("email", email)
                .claim("type", type).claim("role", role)
                .signWith(config.getKey()).compact();
    }

    @Override
    public String createRefreshToken(UUID userId, UUID sessionId, UUID refreshJti, String role, String email) {
        OffsetDateTime now = OffsetDateTime.now();
        String type = "CUSTOMER".equals(role) ? "customer" : "staff";
        return Jwts.builder().subject(userId.toString())
                .issuedAt(Date.from(now.toInstant())).expiration(Date.from(now.plus(config.getRefreshTtl()).toInstant()))
                .claim("sid", sessionId.toString()).claim("jti", refreshJti.toString())
                .claim("email", email)
                .claim("typ", "refresh").claim("type", type).claim("role", role)
                .signWith(config.getKey()).compact();
    }

    @Override
    public String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }
}
