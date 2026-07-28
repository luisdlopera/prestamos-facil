package com.prestamosfacil.domain.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    void shouldCreatePageResult() {
        List<String> items = List.of("a", "b", "c");
        PageResult<String> result = new PageResult<>(items, 0, 10, 3);

        assertEquals(items, result.getContent());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPerPage());
        assertEquals(3, result.getTotal());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void shouldCalculateTotalPages() {
        PageResult<String> result = new PageResult<>(List.of(), 0, 10, 25);
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void shouldHandleZeroTotal() {
        PageResult<String> result = new PageResult<>(List.of(), 0, 10, 0);
        assertEquals(0, result.getTotalPages());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    void shouldHaveNextPage() {
        PageResult<String> result = new PageResult<>(List.of(), 0, 10, 25);
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    void shouldHavePreviousPage() {
        PageResult<String> result = new PageResult<>(List.of(), 2, 10, 25);
        assertTrue(result.hasPrevious());
        assertFalse(result.hasNext());
    }

    @Test
    void shouldHandleZeroPerPage() {
        PageResult<String> result = new PageResult<>(List.of(), 0, 0, 10);
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void shouldHandleExactFit() {
        PageResult<String> result = new PageResult<>(List.of(), 0, 10, 10);
        assertEquals(1, result.getTotalPages());
        assertFalse(result.hasNext());
    }
}
