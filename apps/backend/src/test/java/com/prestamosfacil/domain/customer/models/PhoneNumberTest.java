package com.prestamosfacil.domain.customer.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @Test
    void shouldCreatePhoneNumber() {
        PhoneNumber phone = new PhoneNumber("+57", "3001234567");
        assertEquals("57", phone.getCountryCode());
        assertEquals("3001234567", phone.getNumber());
    }

    @Test
    void shouldReturnFullNumber() {
        PhoneNumber phone = new PhoneNumber("57", "3001234567");
        assertEquals("+573001234567", phone.getFullNumber());
    }

    @Test
    void shouldStripLeadingPlusFromCountryCode() {
        PhoneNumber phone = new PhoneNumber("+57", "3001234567");
        assertEquals("57", phone.getCountryCode());
    }

    @Test
    void shouldStripSpacesFromNumber() {
        PhoneNumber phone = new PhoneNumber("57", "300 123 4567");
        assertEquals("3001234567", phone.getNumber());
    }

    @Test
    void shouldThrowOnNullCountryCode() {
        assertThrows(IllegalArgumentException.class,
            () -> new PhoneNumber(null, "3001234567"));
    }

    @Test
    void shouldThrowOnBlankCountryCode() {
        assertThrows(IllegalArgumentException.class,
            () -> new PhoneNumber("  ", "3001234567"));
    }

    @Test
    void shouldThrowOnNullNumber() {
        assertThrows(IllegalArgumentException.class,
            () -> new PhoneNumber("57", null));
    }

    @Test
    void shouldThrowOnBlankNumber() {
        assertThrows(IllegalArgumentException.class,
            () -> new PhoneNumber("57", "  "));
    }

    @Test
    void shouldCreateEmptyPhoneNumber() {
        PhoneNumber empty = PhoneNumber.empty();
        assertEquals("00", empty.getCountryCode());
        assertEquals("000000000", empty.getNumber());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        PhoneNumber p1 = new PhoneNumber("57", "3001234567");
        PhoneNumber p2 = new PhoneNumber("+57", "3001234567");
        PhoneNumber p3 = new PhoneNumber("1", "5555555555");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    void shouldReturnFormattedToString() {
        PhoneNumber phone = new PhoneNumber("57", "3001234567");
        assertEquals("+573001234567", phone.toString());
    }
}
