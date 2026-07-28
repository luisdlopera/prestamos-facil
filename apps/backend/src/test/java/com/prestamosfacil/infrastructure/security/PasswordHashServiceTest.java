package com.prestamosfacil.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashServiceTest {

    @Test
    void shouldEncodeAndMatchPassword() {
        PasswordHashService service = new PasswordHashService(
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        String hash = service.encode("MyPass1@");
        assertNotNull(hash);
        assertTrue(service.matches("MyPass1@", hash));
        assertFalse(service.matches("WrongPass1@", hash));
    }

    @Test
    void shouldCalculateSha256() {
        PasswordHashService service = new PasswordHashService(
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        String hash = service.sha256("test-value");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
