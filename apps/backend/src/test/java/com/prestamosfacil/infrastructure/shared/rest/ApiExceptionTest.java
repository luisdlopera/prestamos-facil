package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ApiException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void shouldCreateExceptionWithMessageAndStatus() {
        ApiException ex = new ApiException("Not found", org.springframework.http.HttpStatus.NOT_FOUND);
        assertEquals("Not found", ex.getMessage());
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void shouldDefaultToBadRequest() {
        ApiException ex = new ApiException("Bad request", org.springframework.http.HttpStatus.BAD_REQUEST);
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
