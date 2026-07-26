package com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = SwaggerDocs.LOGIN_RESPONSE_STAFF)
public record StaffLoginResponse(
    @Schema(description = SwaggerDocs.STAFF_ID) UUID id,
    @Schema(description = SwaggerDocs.STAFF_NAME, example = SwaggerDocs.EX_STAFF_NAME) String name,
    @Schema(description = SwaggerDocs.STAFF_EMAIL, example = SwaggerDocs.EX_EMAIL_STAFF) String email,
    @Schema(description = "Staff role", example = "ADMIN") String role,
    @Schema(description = SwaggerDocs.STAFF_ENABLED) boolean enabled,
    @Schema(description = "JWT access token") String accessToken,
    @Schema(description = SwaggerDocs.EXPIRES_IN, example = SwaggerDocs.EX_EXPIRES) Long expiresIn
) {}
