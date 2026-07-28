package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import com.prestamosfacil.infrastructure.security.jwt.JwtTokenConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenConfigTest {

    private JwtProperties createProperties(String secret, long accessTtl, long refreshTtl, long skew) {
        return new JwtProperties(
            secret, accessTtl, refreshTtl, skew,
            new JwtProperties.Cookie(true, "Lax", "access_token", "refresh_token", "/", true)
        );
    }

    @Test
    void shouldCreateKeyFromValidSecret() {
        JwtProperties props = createProperties("dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtMzA0NS0yNTYtYml0", 15, 7, 30);
        JwtTokenConfig config = new JwtTokenConfig(props);
        assertNotNull(config.getKey());
        assertEquals(15 * 60, config.getAccessTtl().toSeconds());
        assertEquals(7 * 86400, config.getRefreshTtl().toSeconds());
        assertEquals(30, config.getAllowedClockSkewSeconds());
    }

    @Test
    void shouldThrowWhenSecretInvalid() {
        JwtProperties props = createProperties("REPLACE_ME", 10, 3, 30);
        assertThrows(IllegalStateException.class, () -> new JwtTokenConfig(props));
    }

    @Test
    void shouldUseDefaultMinValues() {
        JwtProperties props = createProperties("dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtMzA0NS0yNTYtYml0", 1, 0, -5);
        JwtTokenConfig config = new JwtTokenConfig(props);
        assertEquals(300, config.getAccessTtl().toSeconds());
        assertEquals(86400, config.getRefreshTtl().toSeconds());
        assertEquals(0, config.getAllowedClockSkewSeconds());
    }
}
