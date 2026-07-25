package com.prestamosfacil.domain.customer.models;

import com.prestamosfacil.domain.customer.constants.CustomerConstants;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.user.models.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class Customer {

    private final UUID id;
    private final User user;
    private final String firstName;
    private final String lastName;
    private final EmailAddress email;
    private final DocumentNumber documentNumber;
    private final PhoneNumber phoneNumber;
    private final Money baseSalary;

    @Builder
    public Customer(UUID id, User user, String firstName, String lastName,
                    EmailAddress email, DocumentNumber documentNumber,
                    PhoneNumber phoneNumber, Money baseSalary) {
        this.id = id != null ? id : UUID.randomUUID();
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.documentNumber = documentNumber;
        this.phoneNumber = phoneNumber != null ? phoneNumber : PhoneNumber.empty();
        this.baseSalary = baseSalary;
        validate();
    }

    public void validate() {
        validateBaseSalary(this.baseSalary);
    }

    private static void validateBaseSalary(Money baseSalary) {
        if (baseSalary != null) {
            var amount = baseSalary.getAmount();
            if (amount.compareTo(BigDecimal.ZERO) < 0 ||
                amount.compareTo(CustomerConstants.MAX_BASE_SALARY) > 0) {
                throw new IllegalArgumentException(
                    Messages.AUTH_SALARY_RANGE.format(CustomerConstants.MAX_BASE_SALARY.toPlainString())
                );
            }
        }
    }
}
