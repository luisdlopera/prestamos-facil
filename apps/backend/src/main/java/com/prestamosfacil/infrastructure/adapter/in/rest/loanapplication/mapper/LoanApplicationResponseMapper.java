package com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.mapper;

import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.dto.response.LoanApplicationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationResponseMapper {
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(app.getCustomer() != null ? app.getCustomer().getFirstName() + \" \" + app.getCustomer().getLastName() : \"\")")
    @Mapping(target = "customerEmail", expression = "java(app.getCustomer() != null && app.getCustomer().getEmail() != null ? app.getCustomer().getEmail().getValue() : null)")
    @Mapping(target = "customerBaseSalary", expression = "java(app.getCustomer() != null && app.getCustomer().getBaseSalary() != null ? app.getCustomer().getBaseSalary().getAmount() : null)")
    @Mapping(target = "loanTypeId", source = "loanType.id")
    @Mapping(target = "loanTypeName", source = "loanType.name")
    @Mapping(target = "requestedAmount", source = "requestedAmount.amount")
    @Mapping(target = "status", expression = "java(app.getStatus().name())")
    @Mapping(target = "decisionReason", source = "decisionReason")
    @Mapping(target = "evaluatedAt", source = "evaluatedAt")
    LoanApplicationResponse toResponse(LoanApplication app);
}
