package com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = SwaggerDocs.TOGGLE_LOAN_TYPE_STATUS_REQUEST)
public record ToggleLoanTypeStatusRequest(
    @Schema(description = SwaggerDocs.LOAN_TYPE_ACTIVE)
    @NotNull(message = ValidationMessages.LOAN_TYPE_STATUS_REQUIRED)
    Boolean active
) {}
