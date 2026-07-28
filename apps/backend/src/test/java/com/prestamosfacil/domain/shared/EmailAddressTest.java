package com.prestamosfacil.domain.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @Test
    void shouldCreateEmailAddress() {
        EmailAddress email = new EmailAddress("test@example.com");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void shouldConvertToLowerCase() {
        EmailAddress email = new EmailAddress("Test@Example.COM");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void shouldTrimEmail() {
        EmailAddress email = new EmailAddress("  test@example.com  ");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void shouldThrowOnNullEmail() {
        assertThrows(IllegalArgumentException.class,
            () -> new EmailAddress(null));
    }

    @Test
    void shouldThrowOnBlankEmail() {
        assertThrows(IllegalArgumentException.class,
            () -> new EmailAddress("  "));
    }

    @Test
    void shouldThrowOnInvalidFormat() {
        assertThrows(IllegalArgumentException.class,
            () -> new EmailAddress("not-an-email"));
    }

    @Test
    void shouldThrowOnMissingDomain() {
        assertThrows(IllegalArgumentException.class,
            () -> new EmailAddress("user@"));
    }

    @Test
    void shouldThrowOnMissingAtSign() {
        assertThrows(IllegalArgumentException.class,
            () -> new EmailAddress("userexample.com"));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        EmailAddress e1 = new EmailAddress("test@example.com");
        EmailAddress e2 = new EmailAddress("TEST@EXAMPLE.COM");
        EmailAddress e3 = new EmailAddress("other@example.com");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }

    @Test
    void shouldReturnEmailInToString() {
        EmailAddress email = new EmailAddress("test@example.com");
        assertEquals("test@example.com", email.toString());
    }
}
