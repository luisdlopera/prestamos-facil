package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.mapper;

import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity.PaymentInstallmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentInstallmentMapper {
    @Mapping(target = "openingBalance", source = "openingBalance", qualifiedByName = "moneyToValueObject")
    @Mapping(target = "paymentAmount", source = "paymentAmount", qualifiedByName = "moneyToValueObject")
    @Mapping(target = "principalAmount", source = "principalAmount", qualifiedByName = "moneyToValueObject")
    @Mapping(target = "interestAmount", source = "interestAmount", qualifiedByName = "moneyToValueObject")
    @Mapping(target = "closingBalance", source = "closingBalance", qualifiedByName = "moneyToValueObject")
    PaymentInstallment toDomain(PaymentInstallmentEntity entity);

    @Mapping(target = "openingBalance", source = "openingBalance", qualifiedByName = "moneyToAmount")
    @Mapping(target = "paymentAmount", source = "paymentAmount", qualifiedByName = "moneyToAmount")
    @Mapping(target = "principalAmount", source = "principalAmount", qualifiedByName = "moneyToAmount")
    @Mapping(target = "interestAmount", source = "interestAmount", qualifiedByName = "moneyToAmount")
    @Mapping(target = "closingBalance", source = "closingBalance", qualifiedByName = "moneyToAmount")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentInstallmentEntity toEntity(PaymentInstallment domain);

    @Named("moneyToValueObject") default Money moneyToValueObject(java.math.BigDecimal a) { return a != null ? new Money(a) : null; }
    @Named("moneyToAmount") default java.math.BigDecimal moneyToAmount(Money m) { return m != null ? m.getAmount() : null; }
}
