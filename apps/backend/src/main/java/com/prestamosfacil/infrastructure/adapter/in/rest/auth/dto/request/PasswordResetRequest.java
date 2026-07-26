package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = SwaggerDocs.PASSWORD_RESET_REQUEST)
public record PasswordResetRequest(
        @NotBlank @Email @Schema(description = SwaggerDocs.EMAIL, example = SwaggerDocs.EX_EMAIL) String email
) {}
