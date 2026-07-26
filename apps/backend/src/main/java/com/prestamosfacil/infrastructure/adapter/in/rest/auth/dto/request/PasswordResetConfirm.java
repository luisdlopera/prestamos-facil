package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = SwaggerDocs.PASSWORD_RESET_CONFIRM)
public record PasswordResetConfirm(
        @NotBlank @Schema(description = SwaggerDocs.RESET_TOKEN, example = SwaggerDocs.EX_RESET_TOKEN) String token,
        @NotBlank @Size(min = 8)
        @Schema(description = SwaggerDocs.NEW_PASSWORD, example = SwaggerDocs.EX_PASSWORD_NEW)
        String newPassword
) {}
