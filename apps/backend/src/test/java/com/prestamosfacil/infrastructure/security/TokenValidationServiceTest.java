package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import com.prestamosfacil.infrastructure.security.jwt.JwtTokenConfig;
import com.prestamosfacil.infrastructure.security.jwt.TokenGenerationService;
import com.prestamosfacil.infrastructure.security.jwt.TokenValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenValidationServiceTest {

    private TokenGenerationService tokenGenerationService;
    private TokenValidationService tokenValidationService;

    @BeforeEach
    void setUp() {
        JwtTokenConfig config = new JwtTokenConfig(new JwtProperties(
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtMzA0NS0yNTYtYml0", 15, 7, 30,
            new JwtProperties.Cookie(false, "Lax", "prestamos_access", "prestamos_refresh", "/", true)));
        tokenGenerationService = new TokenGenerationService(config);
        tokenValidationService = new TokenValidationService(config);
    }

    @Test
    void shouldParseAccessToken() {
        UUID customerId = UUID.randomUUID();
        String token = tokenGenerationService.createAccessToken(customerId, UUID.randomUUID(), "test@test.com", "CUSTOMER");
        Map<String, Object> claims = tokenValidationService.parse(token);

        assertEquals(customerId.toString(), claims.get("sub"));
        assertEquals("customer", claims.get("type"));
        assertEquals("test@test.com", claims.get("email"));
    }

    @Test
    void shouldParseRefreshToken() {
        UUID customerId = UUID.randomUUID();
        String token = tokenGenerationService.createRefreshToken(customerId, UUID.randomUUID(), UUID.randomUUID(), "CUSTOMER", "test@test.com");
        Map<String, Object> claims = tokenValidationService.parse(token);

        assertEquals(customerId.toString(), claims.get("sub"));
        assertEquals("refresh", claims.get("typ"));
        assertEquals("customer", claims.get("type"));
    }

    @Test
    void shouldParseStaffAccessToken() {
        UUID staffId = UUID.randomUUID();
        String token = tokenGenerationService.createAccessToken(staffId, UUID.randomUUID(), "staff@test.com", "ADMIN");
        Map<String, Object> claims = tokenValidationService.parse(token);

        assertEquals(staffId.toString(), claims.get("sub"));
        assertEquals("staff", claims.get("type"));
    }

    @Test
    void shouldParseStaffRefreshToken() {
        UUID staffId = UUID.randomUUID();
        String token = tokenGenerationService.createRefreshToken(staffId, UUID.randomUUID(), UUID.randomUUID(), "ANALYST", "staff@test.com");
        Map<String, Object> claims = tokenValidationService.parse(token);

        assertEquals(staffId.toString(), claims.get("sub"));
        assertEquals("refresh", claims.get("typ"));
        assertEquals("staff", claims.get("type"));
    }
}
