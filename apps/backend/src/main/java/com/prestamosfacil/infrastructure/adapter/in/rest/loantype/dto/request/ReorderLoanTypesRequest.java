package com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(description = SwaggerDocs.REORDER_LOAN_TYPES_REQUEST)
public record ReorderLoanTypesRequest(
    @Schema(description = SwaggerDocs.ID)
    @NotEmpty(message = ValidationMessages.LOAN_TYPE_ORDERED_IDS_REQUIRED)
    List<UUID> orderedIds
) {}
