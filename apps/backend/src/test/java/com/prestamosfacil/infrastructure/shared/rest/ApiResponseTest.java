package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.domain.shared.enums.Messages;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertEquals("hello", response.getData());
        assertEquals(Messages.SUCCESS_OPERATION.getValue(), response.getMessage());
        assertNotNull(response.getTimestamp());
        assertNull(response.getErrors());
    }

    @Test
    void shouldCreateSuccessResponseWithCustomMessage() {
        ApiResponse<String> response = ApiResponse.success("data", "Custom message");

        assertEquals("data", response.getData());
        assertEquals("Custom message", response.getMessage());
    }

    @Test
    void shouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong");

        assertNull(response.getData());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getErrors());
    }

    @Test
    void shouldCreateErrorResponseWithDetails() {
        List<ErrorDetail> errors = List.of(
            new ErrorDetail("NOT_NULL", "must not be null", "field")
        );
        ApiResponse<Void> response = ApiResponse.error("Validation failed", errors);

        assertNull(response.getData());
        assertEquals("Validation failed", response.getMessage());
        assertEquals(1, response.getErrors().size());
        assertEquals("NOT_NULL", response.getErrors().getFirst().code());
    }

    @Test
    void shouldCreatePaginatedResponse() {
        List<String> items = List.of("a", "b");
        var pagination = new PaginatedResponse.Pagination(1, 10, 2, 1, true, false);

        PaginatedResponse<String> response = PaginatedResponse.of(items, pagination);

        assertEquals(items, response.getData());
        assertEquals(1, response.getPagination().page());
        assertTrue(response.getPagination().hasNext());
    }
}
