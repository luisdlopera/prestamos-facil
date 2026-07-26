package com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.request;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = SwaggerDocs.REJECT_REQUEST)
public record RejectRequest(
        @NotBlank @Size(max = 500) @Schema(description = SwaggerDocs.REJECT_REASON, example = SwaggerDocs.EX_REASON_REJECTED) String reason
) {}
