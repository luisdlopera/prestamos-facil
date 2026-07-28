package com.prestamosfacil.domain.auth.models;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    private final Customer customer = Customer.builder()
        .firstName("Juan")
        .lastName("Perez")
        .email(new EmailAddress("juan@test.com"))
        .documentNumber(new DocumentNumber("CC", "123456789"))
        .phoneNumber(new PhoneNumber("+57", "3001234567"))
        .baseSalary(new Money(new BigDecimal("5000000")))
        .user(com.prestamosfacil.domain.user.models.User.builder()
            .email(new EmailAddress("juan@test.com"))
            .passwordHash("hashedpass")
            .build())
        .build();

    @Test
    void shouldBeValidWhenNotRevokedAndNotExpired() {
        RefreshToken token = new RefreshToken(customer, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        assertTrue(token.isValid());
    }

    @Test
    void shouldBeInvalidWhenExpired() {
        RefreshToken token = new RefreshToken(customer, "hash123",
            Instant.now().minus(1, ChronoUnit.DAYS));
        assertFalse(token.isValid());
    }

    @Test
    void shouldBeInvalidWhenRevoked() {
        RefreshToken token = new RefreshToken(customer, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        token = token.revoke();
        assertTrue(token.isRevoked());
        assertFalse(token.isValid());
    }

    @Test
    void shouldRevokeToken() {
        RefreshToken token = new RefreshToken(customer, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        token = token.revoke();
        assertTrue(token.isRevoked());
    }

    @Test
    void shouldReturnCustomerAndTokenHash() {
        RefreshToken token = new RefreshToken(customer, "hash123",
            Instant.now().plus(1, ChronoUnit.DAYS));
        assertEquals(customer, token.getCustomer());
        assertEquals("hash123", token.getTokenHash());
        assertNotNull(token.getExpiresAt());
    }
}
