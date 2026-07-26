package com.prestamosfacil.infrastructure.adapter.in.rest.customer.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = SwaggerDocs.CREATE_CUSTOMER_REQUEST)
public record CreateCustomerRequest(
    @NotBlank @Size(max = 100) @Schema(description = SwaggerDocs.FIRST_NAME, example = SwaggerDocs.EX_FIRST_NAME_ALT) String firstName,
    @NotBlank @Size(max = 100) @Schema(description = SwaggerDocs.LAST_NAME, example = SwaggerDocs.EX_LAST_NAME_ALT) String lastName,
    @NotBlank @Email @Schema(description = SwaggerDocs.EMAIL, example = SwaggerDocs.EX_EMAIL_ALT) String email,
    @NotBlank @Pattern(regexp = "(?i)CC|CE|NIT|TI|PP|PEP") @Schema(description = SwaggerDocs.DOCUMENT_TYPE, example = SwaggerDocs.EX_DOC_TYPE, allowableValues = {"CC", "CE", "NIT", "TI", "PP", "PEP"}) String documentType,
    @NotBlank @Size(max = 50) @Schema(description = SwaggerDocs.DOCUMENT_NUMBER, example = SwaggerDocs.EX_DOC_NUMBER_ALT) String documentNumber,
    @NotNull @DecimalMin("0") @DecimalMax("15000000") @Schema(description = SwaggerDocs.BASE_SALARY, example = SwaggerDocs.EX_SALARY) BigDecimal baseSalary
) {}
