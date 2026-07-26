package com.prestamosfacil.infrastructure.adapter.in.rest.reports.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = SwaggerDocs.APPROVED_LOANS_TOTAL_RESPONSE)
public record ApprovedLoansTotalResponse(
    @Schema(description = SwaggerDocs.TOTAL_APPROVED_AMOUNT, example = SwaggerDocs.EX_AMOUNT_TOTAL) BigDecimal totalApprovedAmount,
    @Schema(description = SwaggerDocs.APPROVED_LOANS_COUNT, example = SwaggerDocs.EX_COUNT) long approvedLoansCount,
    @Schema(description = SwaggerDocs.GENERATED_AT) Instant generatedAt
) {}
