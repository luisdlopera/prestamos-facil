package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleEntityNotFound() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleNotFound(new EntityNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void shouldHandleIllegalArgument() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleIllegalArgument(new IllegalArgumentException("Bad arg"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad arg", response.getBody().getMessage());
    }

    @Test
    void shouldHandleApplicationException() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleApplicationException(new ApplicationException("App error"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("App error", response.getBody().getMessage());
    }

    @Test
    void shouldHandleIllegalState() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleIllegalState(new IllegalStateException("Invalid state"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldHandleGeneralException() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleGeneral(new RuntimeException("Unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody().getMessage());
    }

    @Test
    void shouldHandleAccessDenied() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleAccessDenied(new org.springframework.security.access.AccessDeniedException("Denied"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldHandleAuthenticationException() {
        ResponseEntity<ApiResponse<Void>> response =
            handler.handleAuthentication(new org.springframework.security.core.AuthenticationException("Unauth") {});
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
