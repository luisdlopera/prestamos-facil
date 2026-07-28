package com.prestamosfacil.domain.loan.models;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    private LoanApplication application;
    private Customer customer;
    private LoanType loanType;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("hashedpass")
                .build())
            .build();
        loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        application = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
    }

    private Loan createLoan() {
        return Loan.builder()
            .loanApplication(application)
            .customer(customer)
            .principalAmount(new Money(new BigDecimal("10000000")))
            .annualInterestRate(new BigDecimal("18.5"))
            .termInMonths(12)
            .monthlyPayment(new Money(new BigDecimal("919181.18")))
            .approvedAt(Instant.now())
            .build();
    }

    @Test
    void shouldCreateLoan() {
        Loan loan = createLoan();

        assertNotNull(loan.getId());
        assertNotNull(loan.getApprovedAt());
        assertEquals(application, loan.getLoanApplication());
        assertEquals(customer, loan.getCustomer());
        assertEquals(0, new BigDecimal("10000000").compareTo(loan.getPrincipalAmount().getAmount()));
    }

    @Test
    void shouldCalculateFirstDueDateBasedOnApprovedAt() {
        Loan loan = createLoan();

        LocalDate dueDate = loan.calculateFirstDueDate();
        assertNotNull(dueDate);
        assertEquals(loan.getApprovedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().plusMonths(1), dueDate);
    }

    @Test
    void shouldHaveGetters() {
        Loan loan = createLoan();

        assertEquals(application, loan.getLoanApplication());
        assertEquals(customer, loan.getCustomer());
        assertEquals(0, new BigDecimal("10000000").compareTo(loan.getPrincipalAmount().getAmount()));
        assertEquals(0, new BigDecimal("18.5").compareTo(loan.getAnnualInterestRate()));
        assertEquals(12, loan.getTermInMonths());
    }
}
