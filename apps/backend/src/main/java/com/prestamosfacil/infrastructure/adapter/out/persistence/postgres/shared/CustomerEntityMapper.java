package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity.CustomerEntity;
import java.math.BigDecimal;

public final class CustomerEntityMapper {
    private CustomerEntityMapper() {}

    public static Customer toCustomer(CustomerEntity entity) {
        UserEntity u = entity.getUser();
        User user = User.builder().id(u.getId())
                .email(new EmailAddress(u.getEmail())).passwordHash(u.getPasswordHash())
                .role(u.getRole()).failedLoginAttempts(u.getFailedLoginAttempts())
                .blockedUntil(u.getBlockedUntil()).lastLoginAt(u.getLastLoginAt())
                .enabled(u.isEnabled()).build();
        return Customer.builder().id(entity.getId()).user(user)
                .firstName(entity.getFirstName()).lastName(entity.getLastName())
                .email(new EmailAddress(u.getEmail()))
                .documentNumber(new DocumentNumber(entity.getDocumentType().getCode(), entity.getDocumentNumber()))
                .baseSalary(new Money(entity.getBaseSalary() != null ? entity.getBaseSalary() : BigDecimal.ZERO))
                .build();
    }
}
