package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = SwaggerDocs.REFRESH_RESPONSE)
public record RefreshResponse(
        @Schema(description = "Nuevo JWT access token") String accessToken,
        @Schema(description = SwaggerDocs.EXPIRES_IN_NEW, example = SwaggerDocs.EX_EXPIRES) long expiresIn
) {}
