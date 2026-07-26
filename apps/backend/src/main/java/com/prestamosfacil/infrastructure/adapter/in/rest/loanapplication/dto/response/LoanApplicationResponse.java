package com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = SwaggerDocs.LOAN_APPLICATION_RESPONSE)
public record LoanApplicationResponse(
    @Schema(description = SwaggerDocs.ID) UUID id,
    @Schema(description = SwaggerDocs.CUSTOMER_ID) UUID customerId,
    @Schema(description = SwaggerDocs.CUSTOMER_FULL_NAME, example = SwaggerDocs.EX_FULL_NAME) String customerName,
    @Schema(description = SwaggerDocs.CUSTOMER_EMAIL, example = SwaggerDocs.EX_EMAIL_ALT) String customerEmail,
    @Schema(description = SwaggerDocs.CUSTOMER_BASE_SALARY, example = SwaggerDocs.EX_SALARY) BigDecimal customerBaseSalary,
    @Schema(description = SwaggerDocs.LOAN_TYPE_ID) UUID loanTypeId,
    @Schema(description = SwaggerDocs.LOAN_TYPE_NAME, example = SwaggerDocs.EX_LOAN_TYPE_NAME) String loanTypeName,
    @Schema(description = SwaggerDocs.INTEREST_RATE, example = SwaggerDocs.EX_INTEREST) BigDecimal annualInterestRate,
    @Schema(description = SwaggerDocs.REQUESTED_AMOUNT, example = SwaggerDocs.EX_AMOUNT) BigDecimal requestedAmount,
    @Schema(description = SwaggerDocs.TERM_MONTHS, example = SwaggerDocs.EX_TERM) int termInMonths,
    @Schema(description = SwaggerDocs.LOAN_STATUS, allowableValues = {"PENDING_REVIEW", "MANUAL_REVIEW", "APPROVED", "REJECTED"}) String status,
    @Schema(description = SwaggerDocs.DECISION_REASON_WITH_CONTEXT, example = SwaggerDocs.EX_REASON_APPROVED) String decisionReason,
    @Schema(description = SwaggerDocs.EVALUATED_AT) Instant evaluatedAt,
    @Schema(description = SwaggerDocs.CREATED_AT) Instant createdAt
) {}
