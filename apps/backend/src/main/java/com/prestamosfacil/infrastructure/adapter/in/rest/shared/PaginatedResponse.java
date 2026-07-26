package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = SwaggerDocs.PAGINATED_RESPONSE)
public class PaginatedResponse<T> extends ApiResponse<List<T>> {

    @Schema(description = SwaggerDocs.PAGINATION_INFO)
    private final Pagination pagination;

    private PaginatedResponse(List<T> data, String message, Pagination pagination) {
        super(true, data, message, null);
        this.pagination = pagination;
    }

    public static <T> PaginatedResponse<T> of(List<T> data, Pagination pagination) {
        return new PaginatedResponse<>(data, Messages.SUCCESS_RECORDS_RETRIEVED.getValue(), pagination);
    }

    public static <T> PaginatedResponse<T> of(List<T> data, Pagination pagination, String message) {
        return new PaginatedResponse<>(data, message, pagination);
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Schema(description = SwaggerDocs.PAGINATION_INFO)
    public record Pagination(
        @Schema(description = SwaggerDocs.PAGE_NUMBER, example = SwaggerDocs.EX_PAGE) int page,
        @Schema(description = SwaggerDocs.PAGE_SIZE, example = SwaggerDocs.EX_PAGE_SIZE) int perPage,
        @Schema(description = SwaggerDocs.PAGE_TOTAL, example = SwaggerDocs.EX_TOTAL) long total,
        @Schema(description = SwaggerDocs.PAGE_TOTAL_PAGES, example = SwaggerDocs.EX_PAGES) int totalPages,
        @Schema(description = SwaggerDocs.PAGE_HAS_NEXT, example = SwaggerDocs.EX_TRUE) boolean hasNext,
        @Schema(description = SwaggerDocs.PAGE_HAS_PREVIOUS, example = SwaggerDocs.EX_FALSE) boolean hasPrevious
    ) {}
}
