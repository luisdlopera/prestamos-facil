package com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = SwaggerDocs.REGISTER_REQUEST)
public record RegisterRequest(
        @NotBlank(message = ValidationMessages.FIRST_NAME_REQUIRED) @Size(max = 100) @Schema(description = SwaggerDocs.FIRST_NAME, example = SwaggerDocs.EX_FIRST_NAME) String firstName,
        @NotBlank(message = ValidationMessages.LAST_NAME_REQUIRED) @Size(max = 100) @Schema(description = SwaggerDocs.LAST_NAME, example = SwaggerDocs.EX_LAST_NAME) String lastName,
        @NotBlank(message = ValidationMessages.EMAIL_REQUIRED) @Email(message = ValidationMessages.EMAIL_INVALID) @Schema(description = SwaggerDocs.EMAIL, example = SwaggerDocs.EX_EMAIL) String email,
        @NotBlank(message = ValidationMessages.DOCUMENT_TYPE_REQUIRED) @Pattern(regexp = "(?i)CC|CE|NIT|TI|PP|PEP") @Schema(description = SwaggerDocs.DOCUMENT_TYPE, example = SwaggerDocs.EX_DOC_TYPE, allowableValues = {"CC", "CE", "NIT", "TI", "PP", "PEP"}) String documentType,
        @NotBlank(message = ValidationMessages.DOCUMENT_NUMBER_REQUIRED) @Size(max = 50) @Schema(description = SwaggerDocs.DOCUMENT_NUMBER, example = SwaggerDocs.EX_DOC_NUMBER) String documentNumber,
        @NotNull(message = ValidationMessages.BASE_SALARY_REQUIRED) @DecimalMin(value = "0", message = ValidationMessages.BASE_SALARY_RANGE) @DecimalMax(value = "15000000", message = ValidationMessages.BASE_SALARY_RANGE) @Schema(description = SwaggerDocs.BASE_SALARY, example = SwaggerDocs.EX_SALARY) BigDecimal baseSalary,
        @NotBlank @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
                 message = ValidationMessages.PASSWORD_PATTERN)
        @Schema(description = SwaggerDocs.PASSWORD_PATTERN_HINT, example = SwaggerDocs.EX_PASSWORD)
        String password
) {}
