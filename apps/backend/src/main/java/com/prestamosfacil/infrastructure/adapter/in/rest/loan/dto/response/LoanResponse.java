package com.prestamosfacil.infrastructure.adapter.in.rest.loan.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = SwaggerDocs.LOAN_RESPONSE)
public record LoanResponse(
    @Schema(description = SwaggerDocs.LOAN_ID) UUID id,
    @Schema(description = SwaggerDocs.SOURCE_LOAN_APPLICATION_ID) UUID loanApplicationId,
    @Schema(description = SwaggerDocs.CUSTOMER_ID) UUID customerId,
    @Schema(description = SwaggerDocs.CUSTOMER_NAME) String customerName,
    @Schema(description = SwaggerDocs.CUSTOMER_IDENTIFICATION) String customerIdentification,
    @Schema(description = SwaggerDocs.PRINCIPAL_AMOUNT, example = SwaggerDocs.EX_AMOUNT) BigDecimal principalAmount,
    @Schema(description = SwaggerDocs.INTEREST_RATE, example = SwaggerDocs.EX_INTEREST) BigDecimal annualInterestRate,
    @Schema(description = SwaggerDocs.TERM_MONTHS, example = SwaggerDocs.EX_TERM) int termInMonths,
    @Schema(description = SwaggerDocs.MONTHLY_PAYMENT, example = SwaggerDocs.EX_INSTALLMENT) BigDecimal monthlyPayment,
    @Schema(description = "Estado del préstamo") String status,
    @Schema(description = SwaggerDocs.APPROVED_AT) Instant approvedAt
) {}
