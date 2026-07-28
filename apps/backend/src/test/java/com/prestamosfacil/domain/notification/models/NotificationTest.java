package com.prestamosfacil.domain.notification.models;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldBuildWithData() {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("pass")
                .build())
            .build();

        Notification data = Notification.builder()
            .customer(customer)
            .applicationId("app-123")
            .loanTypeName("Personal")
            .requestedAmount(new BigDecimal("10000000"))
            .termInMonths(12)
            .build();

        assertEquals("app-123", data.getApplicationId());
        assertEquals("Personal", data.getLoanTypeName());
        assertEquals(12, data.getTermInMonths());
    }

    @Test
    void shouldAllowNullFields() {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("pass")
                .build())
            .build();

        Notification data = Notification.builder()
            .customer(customer)
            .applicationId("app-123")
            .reason("Test reason")
            .build();

        assertNull(data.getLoanTypeName());
        assertNull(data.getInstallments());
        assertEquals("Test reason", data.getReason());
    }
}
