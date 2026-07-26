package com.prestamosfacil.infrastructure.adapter.in.rest.loan.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = SwaggerDocs.INSTALLMENT_DESC)
public record PaymentInstallmentResponse(
    @Schema(description = SwaggerDocs.INSTALLMENT_ID) UUID id,
    @Schema(description = SwaggerDocs.INSTALLMENT_NUMBER, example = SwaggerDocs.EX_INSTALLMENT_NUM) int installmentNumber,
    @Schema(description = SwaggerDocs.DUE_DATE, example = SwaggerDocs.EX_DUE_DATE) LocalDate dueDate,
    @Schema(description = SwaggerDocs.OPENING_BALANCE, example = SwaggerDocs.EX_OPENING_BALANCE) BigDecimal openingBalance,
    @Schema(description = SwaggerDocs.PAYMENT_AMOUNT, example = SwaggerDocs.EX_PAYMENT_AMOUNT) BigDecimal paymentAmount,
    @Schema(description = SwaggerDocs.PRINCIPAL_PAYMENT, example = SwaggerDocs.EX_PRINCIPAL_PAYMENT) BigDecimal principalAmount,
    @Schema(description = SwaggerDocs.INTEREST_PAYMENT, example = SwaggerDocs.EX_INTEREST_PAYMENT) BigDecimal interestAmount,
    @Schema(description = SwaggerDocs.CLOSING_BALANCE, example = SwaggerDocs.EX_CLOSING_BALANCE) BigDecimal closingBalance
) {}
