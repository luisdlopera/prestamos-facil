package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import com.prestamosfacil.infrastructure.security.jwt.JwtTokenConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenConfigTest {

    private JwtProperties createProperties(String secret, long accessTtl, long refreshTtl, long skew) {
        return new JwtProperties(
            secret, accessTtl, refreshTtl, skew,
            new JwtProperties.Cookie(true, "Lax", "access_token", "refresh_token", "/", true)
        );
    }

    private MockEnvironment nonProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        return env;
    }

    @Test
    void shouldCreateKeyFromValidSecret() {
        JwtProperties props = createProperties("dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtMzA0NS0yNTYtYml0", 15, 7, 30);
        JwtTokenConfig config = new JwtTokenConfig(props, nonProdEnvironment());
        assertNotNull(config.getKey());
        assertEquals(15 * 60, config.getAccessTtl().toSeconds());
        assertEquals(7 * 86400, config.getRefreshTtl().toSeconds());
        assertEquals(30, config.getAllowedClockSkewSeconds());
    }

    @Test
    void shouldThrowWhenSecretInvalid() {
        JwtProperties props = createProperties("REPLACE_ME", 10, 3, 30);
        assertThrows(IllegalStateException.class, () -> new JwtTokenConfig(props, nonProdEnvironment()));
    }

    @Test
    void shouldUseDefaultMinValues() {
        JwtProperties props = createProperties("dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtMzA0NS0yNTYtYml0", 1, 0, -5);
        JwtTokenConfig config = new JwtTokenConfig(props, nonProdEnvironment());
        assertEquals(300, config.getAccessTtl().toSeconds());
        assertEquals(86400, config.getRefreshTtl().toSeconds());
        assertEquals(0, config.getAllowedClockSkewSeconds());
    }

    @Test
    void shouldThrowInProdProfileWithoutExplicitJwtSecretEnvVar() {
        String existing = System.getenv("JWT_SECRET");
        Assumptions.assumeTrue(existing == null || existing.isBlank(),
            "Skipping: JWT_SECRET is set in this environment");
        JwtProperties props = createProperties(
            "dev-secret-replace-in-production-at-least-32-chars", 15, 7, 30);
        MockEnvironment prodEnv = new MockEnvironment();
        prodEnv.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class, () -> new JwtTokenConfig(props, prodEnv));
    }
}
