package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = SwaggerDocs.CHANGE_PASSWORD_REQUEST)
public record ChangePasswordRequest(
        @NotBlank @Schema(description = SwaggerDocs.CURRENT_PASSWORD, example = SwaggerDocs.EX_PASSWORD) String currentPassword,
        @NotBlank @Size(min = 8)
        @Schema(description = SwaggerDocs.NEW_PASSWORD, example = SwaggerDocs.EX_PASSWORD_NEW)
        String newPassword
) {}
