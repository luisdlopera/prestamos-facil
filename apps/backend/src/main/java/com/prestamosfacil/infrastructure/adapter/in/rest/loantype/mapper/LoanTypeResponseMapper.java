package com.prestamosfacil.infrastructure.adapter.in.rest.loantype.mapper;

import com.prestamosfacil.domain.loantype.enums.RateType;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request.CreateLoanTypeRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.response.LoanTypeResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.dto.request.UpdateLoanTypeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LoanTypeResponseMapper {
    @Mapping(target = "rateType", source = "rateType", qualifiedByName = "rateTypeToName")
    @Mapping(target = "minAmount", source = "minAmount.amount")
    @Mapping(target = "maxAmount", source = "maxAmount.amount")
    LoanTypeResponse toResponse(LoanType loanType);

    default List<LoanTypeResponse> toResponseList(List<LoanType> loanTypes) { if (loanTypes == null) return List.of(); return loanTypes.stream().map(this::toResponse).toList(); }

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "name", expression = "java(req.name() != null ? req.name().trim() : \"\")")
    @Mapping(target = "description", expression = "java(req.description() != null ? req.description().trim() : \"\")")
    @Mapping(target = "minAmount", source = "minAmount", qualifiedByName = "bigDecimalToMoney")
    @Mapping(target = "maxAmount", source = "maxAmount", qualifiedByName = "bigDecimalToMoney")
    @Mapping(target = "rateType", source = "rateType", qualifiedByName = "stringToRateType")
    @Mapping(target = "automaticValidationEnabled", expression = "java(req.automaticValidationEnabled() != null ? req.automaticValidationEnabled() : false)")
    @Mapping(target = "active", expression = "java(req.active() != null ? req.active() : true)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoanType toDomain(CreateLoanTypeRequest req);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", expression = "java(req.name() != null ? req.name().trim() : \"\")")
    @Mapping(target = "description", expression = "java(req.description() != null ? req.description().trim() : \"\")")
    @Mapping(target = "minAmount", source = "req.minAmount", qualifiedByName = "bigDecimalToMoney")
    @Mapping(target = "maxAmount", source = "req.maxAmount", qualifiedByName = "bigDecimalToMoney")
    @Mapping(target = "rateType", source = "req.rateType", qualifiedByName = "stringToRateType")
    @Mapping(target = "automaticValidationEnabled", source = "req.automaticValidationEnabled")
    @Mapping(target = "active", source = "req.active")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoanType toDomain(UUID id, UpdateLoanTypeRequest req);

    @Named("rateTypeToName") default String rateTypeToName(RateType rt) { return rt != null ? rt.name() : "EA"; }
    @Named("stringToRateType") default RateType stringToRateType(String v) { return v != null ? RateType.fromString(v) : RateType.EA; }
    @Named("bigDecimalToMoney") default Money bigDecimalToMoney(java.math.BigDecimal v) { return v != null ? new Money(v) : null; }
}
