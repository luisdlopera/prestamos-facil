package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import com.prestamosfacil.infrastructure.security.jwt.JwtTokenConfig;
import com.prestamosfacil.infrastructure.security.jwt.TokenGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenGenerationServiceTest {

    private TokenGenerationService tokenGenerationService;

    @BeforeEach
    void setUp() {
        JwtTokenConfig config = new JwtTokenConfig(new JwtProperties(
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtMzA0NS0yNTYtYml0", 15, 7, 30,
            new JwtProperties.Cookie(false, "Lax", "prestamos_access", "prestamos_refresh", "/", true)),
            new MockEnvironment());
        tokenGenerationService = new TokenGenerationService(config);
    }

    @Test
    void shouldCreateAccessToken() {
        String token = tokenGenerationService.createAccessToken(
            UUID.randomUUID(), UUID.randomUUID(), "test@test.com", "CUSTOMER");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldCreateRefreshToken() {
        String token = tokenGenerationService.createRefreshToken(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CUSTOMER", "test@test.com");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldCreateStaffAccessToken() {
        String token = tokenGenerationService.createAccessToken(
            UUID.randomUUID(), UUID.randomUUID(), "staff@test.com", "ADMIN");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldCreateStaffRefreshToken() {
        String token = tokenGenerationService.createRefreshToken(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "ANALYST", "staff@test.com");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }
}
