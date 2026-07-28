package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.shared.rest.PaginatedResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ResponseHelper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseHelperTest {

    @Test
    void shouldCreatePaginatedFromPageResult() {
        List<String> items = List.of("a", "b");
        PageResult<String> pageResult = new PageResult<>(items, 0, 10, 2);
        PaginatedResponse<String> response = ResponseHelper.paginated(pageResult, PageRequest.of(0, 10));
        assertEquals(2, response.getData().size());
        assertEquals(1, response.getPagination().totalPages());
    }
}
