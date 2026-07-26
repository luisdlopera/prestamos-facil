package com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = SwaggerDocs.CREATE_LOAN_APPLICATION_REQUEST)
public record CreateLoanApplicationRequest(
    @Schema(description = "ID del cliente (solo para staff/admin)") UUID customerId,
    @NotNull @Schema(description = SwaggerDocs.LOAN_TYPE_ID) UUID loanTypeId,
    @NotNull @DecimalMin("0.01") @Schema(description = SwaggerDocs.REQUESTED_AMOUNT, example = SwaggerDocs.EX_AMOUNT) BigDecimal requestedAmount,
    @NotNull @Min(1) @Max(360) @Schema(description = SwaggerDocs.TERM_MONTHS, example = SwaggerDocs.EX_TERM) int termInMonths
) {}
