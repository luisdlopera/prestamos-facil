package com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = SwaggerDocs.STAFF_REGISTER_REQUEST)
public record StaffRegisterRequest(
    @NotBlank @Schema(description = SwaggerDocs.PERSONAL_NAME, example = SwaggerDocs.EX_STAFF_NAME) String name,
    @NotBlank @Email @Schema(description = SwaggerDocs.STAFF_EMAIL, example = SwaggerDocs.EX_EMAIL_STAFF) String email,
    @NotBlank @Size(min = 6) @Schema(description = SwaggerDocs.STAFF_PASSWORD) String password,
    @Schema(description = SwaggerDocs.STAFF_ROLE, example = SwaggerDocs.EX_ROLE, allowableValues = {"ADMIN", "AGENT"}) String role
) {}
