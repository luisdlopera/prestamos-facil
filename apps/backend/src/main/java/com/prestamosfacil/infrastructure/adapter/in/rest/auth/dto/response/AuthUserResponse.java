package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = SwaggerDocs.AUTH_USER_RESPONSE)
public record AuthUserResponse(
        @Schema(description = SwaggerDocs.USER_ID) UUID id,
        @Schema(description = SwaggerDocs.USER_FIRST_NAME, example = SwaggerDocs.EX_FIRST_NAME) String firstName,
        @Schema(description = SwaggerDocs.USER_LAST_NAME, example = SwaggerDocs.EX_LAST_NAME) String lastName,
        @Schema(description = SwaggerDocs.EMAIL, example = SwaggerDocs.EX_EMAIL) String email,
        @Schema(description = SwaggerDocs.USER_DOCUMENT_TYPE, example = SwaggerDocs.EX_DOC_TYPE) String documentType,
        @Schema(description = SwaggerDocs.DOCUMENT_NUMBER, example = SwaggerDocs.EX_DOC_NUMBER) String documentNumber,
        @Schema(description = SwaggerDocs.BASE_SALARY, example = SwaggerDocs.EX_SALARY) BigDecimal baseSalary,
        @Schema(description = SwaggerDocs.USER_TYPE, allowableValues = {"customer", "staff"}, example = SwaggerDocs.EX_USER_TYPE) String userType,
        @Schema(description = "Application role", example = "ADMIN") String role
) {}
