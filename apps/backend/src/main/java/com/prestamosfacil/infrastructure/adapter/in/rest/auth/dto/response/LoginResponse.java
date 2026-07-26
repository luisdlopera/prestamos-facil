package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = SwaggerDocs.LOGIN_RESPONSE)
public record LoginResponse(
        @Schema(description = SwaggerDocs.AUTH_USER_RESPONSE) AuthUserResponse user,
        @Schema(description = "JWT access token") String accessToken,
        @Schema(description = SwaggerDocs.EXPIRES_IN, example = SwaggerDocs.EX_EXPIRES) long expiresIn
) {}
