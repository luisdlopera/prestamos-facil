package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = SwaggerDocs.UPDATE_PROFILE_REQUEST)
public record UpdateProfileRequest(
        @Size(max = 100) @Schema(description = SwaggerDocs.NEW_FIRST_NAME, example = SwaggerDocs.EX_FIRST_NAME) String firstName,
        @Size(max = 100) @Schema(description = SwaggerDocs.NEW_LAST_NAME, example = SwaggerDocs.EX_LAST_NAME) String lastName,
        @DecimalMin("0") @DecimalMax("15000000") @Schema(description = SwaggerDocs.NEW_BASE_SALARY, example = SwaggerDocs.EX_SALARY_NEW) BigDecimal baseSalary,
        @Email @Size(max = 255) @Schema(description = SwaggerDocs.NEW_EMAIL, example = SwaggerDocs.EX_EMAIL_NEW) String email
) {}
