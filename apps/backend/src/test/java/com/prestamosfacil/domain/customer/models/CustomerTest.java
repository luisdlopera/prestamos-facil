package com.prestamosfacil.domain.customer.models;

import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithValidSalary() {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .build();

        assertNotNull(customer);
        assertEquals(new BigDecimal("5000000.00"), customer.getBaseSalary().getAmount());
    }

    @Test
    void shouldThrowExceptionWhenSalaryExceedsMaxLimit() {
        assertThrows(IllegalArgumentException.class, () ->
            Customer.builder()
                .firstName("Juan")
                .baseSalary(new Money(new BigDecimal("20000000")))
                .build()
        );
    }
}
