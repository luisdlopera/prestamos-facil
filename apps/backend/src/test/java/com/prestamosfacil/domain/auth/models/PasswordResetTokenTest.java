package com.prestamosfacil.domain.auth.models;

import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.user.models.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    private final User user = User.builder()
        .email(new EmailAddress("juan@test.com"))
        .passwordHash("hashedpass")
        .build();

    @Test
    void shouldBeValidWhenNotUsedAndNotExpired() {
        PasswordResetToken token = new PasswordResetToken(user, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        assertTrue(token.isValid());
    }

    @Test
    void shouldBeInvalidWhenExpired() {
        PasswordResetToken token = new PasswordResetToken(user, "hash123",
            Instant.now().minus(1, ChronoUnit.DAYS));
        assertFalse(token.isValid());
    }

    @Test
    void shouldBeInvalidWhenUsed() {
        PasswordResetToken token = new PasswordResetToken(user, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        token = token.markUsed();
        assertTrue(token.isUsed());
        assertFalse(token.isValid());
    }

    @Test
    void shouldMarkAsUsed() {
        PasswordResetToken token = new PasswordResetToken(user, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        token = token.markUsed();
        assertTrue(token.isUsed());
    }

    @Test
    void shouldReturnUserAndTokenHash() {
        PasswordResetToken token = new PasswordResetToken(user, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        assertEquals(user, token.getUser());
        assertEquals("hash123", token.getTokenHash());
        assertNotNull(token.getExpiresAt());
    }
}
