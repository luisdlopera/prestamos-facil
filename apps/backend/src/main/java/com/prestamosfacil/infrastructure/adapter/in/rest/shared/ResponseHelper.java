package com.prestamosfacil.infrastructure.adapter.in.rest.shared;

import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.shared.rest.PaginatedResponse;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public final class ResponseHelper {

    private ResponseHelper() {}

    public static <T> PaginatedResponse<T> paginated(PageResult<T> pageResult, PageRequest pageRequest) {
        var springPage = new PageImpl<>(pageResult.getContent(), pageRequest, pageResult.getTotal());
        var page = springPage;
        return PaginatedResponse.of(page.getContent(),
            new PaginatedResponse.Pagination(page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.hasNext(), page.hasPrevious()));
    }
}
