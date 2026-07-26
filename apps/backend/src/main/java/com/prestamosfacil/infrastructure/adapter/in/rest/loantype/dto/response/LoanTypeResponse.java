package com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = SwaggerDocs.LOAN_TYPE_RESPONSE)
public record LoanTypeResponse(
    @Schema(description = SwaggerDocs.ID) UUID id,
    @Schema(description = SwaggerDocs.LOAN_TYPE_NAME, example = SwaggerDocs.EX_LOAN_TYPE_NAME) String name,
    @Schema(description = SwaggerDocs.LOAN_TYPE_DESCRIPTION) String description,
    @Schema(description = SwaggerDocs.INTEREST_RATE_PERCENTAGE, example = SwaggerDocs.EX_INTEREST) BigDecimal interestRate,
    @Schema(description = SwaggerDocs.RATE_TYPE) String rateType,
    @Schema(description = SwaggerDocs.MIN_AMOUNT, example = SwaggerDocs.EX_AMOUNT_MIN) BigDecimal minAmount,
    @Schema(description = SwaggerDocs.MAX_AMOUNT, example = SwaggerDocs.EX_AMOUNT_MAX) BigDecimal maxAmount,
    @Schema(description = SwaggerDocs.MIN_TERM_MONTHS, example = SwaggerDocs.EX_TERM_MIN) int minTermMonths,
    @Schema(description = SwaggerDocs.MAX_TERM_MONTHS, example = SwaggerDocs.EX_TERM_MAX) int maxTermMonths,
    @Schema(description = SwaggerDocs.DISPLAY_ORDER) int displayOrder,
    @Schema(description = SwaggerDocs.LOAN_TYPE_ACTIVE) boolean active,
    @Schema(description = SwaggerDocs.AUTO_VALIDATION_ENABLED) boolean automaticValidationEnabled,
    @Schema(description = SwaggerDocs.CREATED_AT) Instant createdAt,
    @Schema(description = SwaggerDocs.UPDATED_AT) Instant updatedAt
) {}
