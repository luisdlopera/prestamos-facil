package com.prestamosfacil.domain.loanapplication.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidLoanApplicationStateExceptionTest {

    @Test
    void shouldHaveCorrectMessage() {
        String msg = "Invalid state";
        InvalidLoanApplicationStateException ex = new InvalidLoanApplicationStateException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldExtendRuntimeException() {
        assertInstanceOf(RuntimeException.class, new InvalidLoanApplicationStateException("test"));
    }
}
