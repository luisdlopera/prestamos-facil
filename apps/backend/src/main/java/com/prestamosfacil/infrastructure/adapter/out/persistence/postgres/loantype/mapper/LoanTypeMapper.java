package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.mapper;

import com.prestamosfacil.domain.loantype.enums.RateType;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.entity.LoanTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LoanTypeMapper {
    @Mapping(target = "interestRate", source = "annualInterestRate")
    @Mapping(target = "rateType", source = "rateType", qualifiedByName = "rateTypeFromString")
    @Mapping(target = "minAmount", source = "minimumAmount", qualifiedByName = "moneyToValueObject")
    @Mapping(target = "maxAmount", source = "maximumAmount", qualifiedByName = "moneyToValueObject")
    @Mapping(target = "minTermMonths", source = "minimumTermMonths")
    @Mapping(target = "maxTermMonths", source = "maximumTermMonths")
    LoanType toDomain(LoanTypeEntity entity);

    @Mapping(target = "annualInterestRate", source = "interestRate")
    @Mapping(target = "rateType", source = "rateType", qualifiedByName = "rateTypeToString")
    @Mapping(target = "minimumAmount", source = "minAmount", qualifiedByName = "moneyToAmount")
    @Mapping(target = "maximumAmount", source = "maxAmount", qualifiedByName = "moneyToAmount")
    @Mapping(target = "minimumTermMonths", source = "minTermMonths")
    @Mapping(target = "maximumTermMonths", source = "maxTermMonths")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoanTypeEntity toEntity(LoanType domain);

    @Named("rateTypeFromString") default RateType rateTypeFromString(String v) { return v != null ? RateType.fromString(v) : RateType.EA; }
    @Named("rateTypeToString") default String rateTypeToString(RateType v) { return v != null ? v.name() : "EA"; }
    @Named("moneyToValueObject") default Money moneyToValueObject(java.math.BigDecimal a) { return a != null ? new Money(a) : null; }
    @Named("moneyToAmount") default java.math.BigDecimal moneyToAmount(Money m) { return m != null ? m.getAmount() : null; }
}
