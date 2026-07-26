package com.prestamosfacil.infrastructure.adapter.in.rest.customer.mapper;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.infrastructure.adapter.in.rest.customer.dto.response.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerResponseMapper {
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "documentType", source = "documentNumber.type")
    @Mapping(target = "documentNumber", source = "documentNumber.number")
    @Mapping(target = "baseSalary", source = "baseSalary.amount")
    @Mapping(target = "phoneCountryCode", constant = "+57")
    @Mapping(target = "phoneNumber", constant = "")
    CustomerResponse toResponse(Customer c);
}
