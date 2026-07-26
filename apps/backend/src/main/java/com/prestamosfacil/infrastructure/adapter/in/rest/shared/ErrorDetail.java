package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = SwaggerDocs.ERROR_DETAIL)
public record ErrorDetail(
    @Schema(description = SwaggerDocs.ERROR_CODE, example = SwaggerDocs.EX_ERROR_CODE) String code,
    @Schema(description = SwaggerDocs.RESPONSE_MESSAGE, example = SwaggerDocs.EX_ERROR_MSG) String message,
    @Schema(description = SwaggerDocs.ERROR_FIELD, example = SwaggerDocs.EX_ERROR_FIELD) String field
) {}
