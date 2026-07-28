package com.prestamosfacil.infrastructure.shared.rest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ErrorDetailTest {

    @Test
    void shouldCreateErrorDetail() {
        ErrorDetail detail = new ErrorDetail("NOT_NULL", "must not be null", "field");
        assertEquals("NOT_NULL", detail.code());
        assertEquals("must not be null", detail.message());
        assertEquals("field", detail.field());
    }
}
