package com.prestamosfacil.application.shared;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        ApplicationException ex = new ApplicationException("Test error");
        assertEquals("Test error", ex.getMessage());
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        ApplicationException ex = new ApplicationException("Test error", cause);
        assertEquals("Test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
