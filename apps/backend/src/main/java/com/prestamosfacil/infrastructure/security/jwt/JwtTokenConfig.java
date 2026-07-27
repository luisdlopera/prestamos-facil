package com.prestamosfacil.infrastructure.security.jwt;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenConfig {
    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final long allowedClockSkewSeconds;

    public JwtTokenConfig(JwtProperties properties) {
        byte[] bytes = null;
        try { bytes = Decoders.BASE64URL.decode(properties.jwtSecret()); }
        catch (Exception e) { try { bytes = Decoders.BASE64.decode(properties.jwtSecret()); } catch (Exception ex) {} }
        if (bytes == null || bytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 256-bit (32 bytes) base64-encoded. " +
                "Configure 'app.security.jwt-secret' or JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTtl = Duration.ofMinutes(Math.max(5, properties.accessTokenTtlMinutes()));
        this.refreshTtl = Duration.ofDays(Math.max(1, properties.refreshTokenTtlDays()));
        this.allowedClockSkewSeconds = Math.max(0, properties.clockSkewSeconds());
    }
    public SecretKey getKey() { return key; }
    public Duration getAccessTtl() { return accessTtl; }
    public Duration getRefreshTtl() { return refreshTtl; }
    public long getAllowedClockSkewSeconds() { return allowedClockSkewSeconds; }
}
