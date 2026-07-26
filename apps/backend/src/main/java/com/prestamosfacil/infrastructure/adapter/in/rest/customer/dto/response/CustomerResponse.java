package com.prestamosfacil.infrastructure.adapter.in.rest.customer.dto.response;

import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = SwaggerDocs.CUSTOMER_RESPONSE)
public record CustomerResponse(
    @Schema(description = SwaggerDocs.USER_ID) UUID id,
    @Schema(description = SwaggerDocs.FIRST_NAME, example = SwaggerDocs.EX_FIRST_NAME_ALT) String firstName,
    @Schema(description = SwaggerDocs.LAST_NAME, example = SwaggerDocs.EX_LAST_NAME_ALT) String lastName,
    @Schema(description = SwaggerDocs.EMAIL, example = SwaggerDocs.EX_EMAIL_ALT) String email,
    @Schema(description = SwaggerDocs.DOCUMENT_TYPE, example = SwaggerDocs.EX_DOC_TYPE) String documentType,
    @Schema(description = SwaggerDocs.DOCUMENT_NUMBER, example = SwaggerDocs.EX_DOC_NUMBER_ALT) String documentNumber,
    @Schema(description = SwaggerDocs.PHONE_COUNTRY_CODE, example = SwaggerDocs.EX_PHONE_CODE) String phoneCountryCode,
    @Schema(description = SwaggerDocs.PHONE_NUMBER, example = SwaggerDocs.EX_PHONE_NUMBER) String phoneNumber,
    @Schema(description = SwaggerDocs.BASE_SALARY, example = SwaggerDocs.EX_SALARY) BigDecimal baseSalary
) {}
