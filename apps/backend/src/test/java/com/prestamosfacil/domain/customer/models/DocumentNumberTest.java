package com.prestamosfacil.domain.customer.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentNumberTest {

    @Test
    void shouldCreateDocumentNumber() {
        DocumentNumber doc = new DocumentNumber("CC", "123456789");
        assertEquals("CC", doc.getType());
        assertEquals("123456789", doc.getNumber());
    }

    @Test
    void shouldTrimAndUpperCaseType() {
        DocumentNumber doc = new DocumentNumber("  cc  ", "123456789");
        assertEquals("CC", doc.getType());
    }

    @Test
    void shouldTrimNumber() {
        DocumentNumber doc = new DocumentNumber("CC", "  123456789  ");
        assertEquals("123456789", doc.getNumber());
    }

    @Test
    void shouldThrowOnNullType() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentNumber(null, "123456789"));
    }

    @Test
    void shouldThrowOnBlankType() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentNumber("  ", "123456789"));
    }

    @Test
    void shouldThrowOnNullNumber() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentNumber("CC", null));
    }

    @Test
    void shouldThrowOnBlankNumber() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentNumber("CC", "  "));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        DocumentNumber doc1 = new DocumentNumber("CC", "123456789");
        DocumentNumber doc2 = new DocumentNumber("CC", "123456789");
        DocumentNumber doc3 = new DocumentNumber("NIT", "987654321");

        assertEquals(doc1, doc2);
        assertEquals(doc1.hashCode(), doc2.hashCode());
        assertNotEquals(doc1, doc3);
    }

    @Test
    void shouldReturnFormattedToString() {
        DocumentNumber doc = new DocumentNumber("CC", "123456789");
        assertEquals("CC 123456789", doc.toString());
    }
}
