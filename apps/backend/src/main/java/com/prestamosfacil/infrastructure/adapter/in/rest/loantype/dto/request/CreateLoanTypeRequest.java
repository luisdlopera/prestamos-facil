package com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(description = SwaggerDocs.CREATE_LOAN_TYPE_REQUEST)
public record CreateLoanTypeRequest(
    @Schema(description = SwaggerDocs.LOAN_TYPE_NAME, example = SwaggerDocs.EX_LOAN_TYPE_NAME)
    @NotBlank(message = ValidationMessages.LOAN_TYPE_NAME_REQUIRED)
    String name,

    @Schema(description = SwaggerDocs.LOAN_TYPE_DESCRIPTION)
    String description,

    @Schema(description = SwaggerDocs.INTEREST_RATE_PERCENTAGE, example = SwaggerDocs.EX_INTEREST)
    @NotNull(message = ValidationMessages.LOAN_TYPE_INTEREST_RATE_REQUIRED)
    @DecimalMin(value = "0.00", message = ValidationMessages.LOAN_TYPE_INTEREST_RATE_NEGATIVE)
    @DecimalMax(value = "100.00", message = ValidationMessages.LOAN_TYPE_INTEREST_RATE_MAX)
    BigDecimal interestRate,

    @Schema(description = SwaggerDocs.RATE_TYPE)
    @NotBlank(message = ValidationMessages.LOAN_TYPE_RATE_TYPE_REQUIRED)
    @Pattern(regexp = "(?i)EA", message = "El único tipo de tasa soportado actualmente es EA")
    String rateType,

    @Schema(description = SwaggerDocs.MIN_AMOUNT, example = SwaggerDocs.EX_AMOUNT_MIN)
    @NotNull(message = ValidationMessages.LOAN_TYPE_MIN_AMOUNT_REQUIRED)
    @DecimalMin(value = "0.01", message = ValidationMessages.LOAN_TYPE_MIN_AMOUNT_POSITIVE)
    BigDecimal minAmount,

    @Schema(description = SwaggerDocs.MAX_AMOUNT, example = SwaggerDocs.EX_AMOUNT_MAX)
    @NotNull(message = ValidationMessages.LOAN_TYPE_MAX_AMOUNT_REQUIRED)
    @DecimalMin(value = "0.01", message = ValidationMessages.LOAN_TYPE_MAX_AMOUNT_POSITIVE)
    BigDecimal maxAmount,

    @Schema(description = SwaggerDocs.MIN_TERM_MONTHS, example = SwaggerDocs.EX_TERM_MIN)
    @Min(value = 1, message = ValidationMessages.LOAN_TYPE_MIN_TERM_REQUIRED)
    int minTermMonths,

    @Schema(description = SwaggerDocs.MAX_TERM_MONTHS, example = SwaggerDocs.EX_TERM_MAX)
    @Min(value = 1, message = ValidationMessages.LOAN_TYPE_MAX_TERM_REQUIRED)
    int maxTermMonths,

    @Schema(description = SwaggerDocs.DISPLAY_ORDER)
    @Min(value = 0, message = ValidationMessages.LOAN_TYPE_DISPLAY_ORDER_MIN)
    int displayOrder,

    @Schema(description = SwaggerDocs.LOAN_TYPE_ACTIVE)
    Boolean active,

    @Schema(description = SwaggerDocs.AUTO_VALIDATION_ENABLED)
    Boolean automaticValidationEnabled
) {}
