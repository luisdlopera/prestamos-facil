package com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = SwaggerDocs.AUTOMATIC_EVALUATION_RESPONSE)
public record AutomaticEvaluationResponse(
    @Schema(description = SwaggerDocs.LOAN_APPLICATION_ID) UUID applicationId,
    @Schema(description = SwaggerDocs.DECISION, allowableValues = {"APPROVED", "REJECTED", "MANUAL_REVIEW"}) String decision,
    @Schema(description = SwaggerDocs.MAX_CAPACITY, example = SwaggerDocs.EX_MAX_CAPACITY) BigDecimal maxCapacity,
    @Schema(description = SwaggerDocs.CURRENT_DEBT, example = SwaggerDocs.EX_CURRENT_DEBT) BigDecimal currentDebt,
    @Schema(description = SwaggerDocs.NEW_INSTALLMENT_AMOUNT, example = SwaggerDocs.EX_INSTALLMENT) BigDecimal newInstallment,
    @Schema(description = SwaggerDocs.DEBT_RATIO, example = SwaggerDocs.EX_DEBT_RATIO) BigDecimal debtRatio,
    @Schema(description = SwaggerDocs.DECISION_REASON, example = SwaggerDocs.EX_REASON_APPROVED) String reason
) {}
