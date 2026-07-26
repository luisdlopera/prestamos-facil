package com.prestamosfacil.infrastructure.adapter.in.rest.loan.mapper;

import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.infrastructure.adapter.in.rest.loan.dto.response.LoanResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.loan.dto.response.PaymentInstallmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanResponseMapper {
    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(loan.getCustomer().getFirstName() + \" \" + loan.getCustomer().getLastName())")
    @Mapping(target = "customerIdentification", expression = "java(loan.getCustomer().getDocumentNumber().toString())")
    @Mapping(target = "principalAmount", source = "principalAmount.amount")
    @Mapping(target = "monthlyPayment", source = "monthlyPayment.amount")
    LoanResponse toResponse(Loan loan);

    @Mapping(target = "openingBalance", source = "openingBalance.amount")
    @Mapping(target = "paymentAmount", source = "paymentAmount.amount")
    @Mapping(target = "principalAmount", source = "principalAmount.amount")
    @Mapping(target = "interestAmount", source = "interestAmount.amount")
    @Mapping(target = "closingBalance", source = "closingBalance.amount")
    PaymentInstallmentResponse toInstallmentResponse(PaymentInstallment pi);
}
