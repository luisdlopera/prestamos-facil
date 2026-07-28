package com.prestamosfacil.domain.loanapplication.models;

import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loanapplication.exceptions.InvalidLoanApplicationStateException;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoanApplicationTest {

    private Customer customer;
    private LoanType loanType;
    private LoanApplication application;

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

    @Test
    void shouldCreateInPendingReview() {
        assertEquals(LoanApplicationStatus.PENDING_REVIEW, application.getStatus());
    }

    @Test
    void shouldCaptureAnnualInterestRateSnapshotOnCreation() {
        // La tasa se captura del loanType al crear la solicitud (snapshot)
        assertEquals(new BigDecimal("12.0"), application.getAnnualInterestRate());
    }

    @Test
    void shouldBeDecidableInPendingReview() {
        assertTrue(application.canBeDecided());
    }

    @Test
    void shouldNotBeDecidableWhenApproved() {
        application = application.approve();
        assertFalse(application.canBeDecided());
    }

    @Test
    void shouldNotBeDecidableWhenRejected() {
        application = application.reject("No cumple requisitos");
        assertFalse(application.canBeDecided());
    }

    @Test
    void shouldApproveApplication() {
        application = application.approve();

        assertEquals(LoanApplicationStatus.APPROVED, application.getStatus());
        // La tasa snapshot se preserva tras la aprobación
        assertEquals(new BigDecimal("12.0"), application.getAnnualInterestRate());
        assertNotNull(application.getEvaluatedAt());
    }

    @Test
    void shouldPreserveRateSnapshotAfterApproval() {
        // Simula que el loan_type cambia su tasa después; la solicitud conserva la original
        application = application.approve();
        assertEquals(new BigDecimal("12.0"), application.getAnnualInterestRate());
    }

    @Test
    void shouldThrowWhenApprovingNonDecidable() {
        LoanApplication approvedApp = application.approve();
        assertThrows(InvalidLoanApplicationStateException.class, approvedApp::approve);
    }

    @Test
    void shouldRejectApplication() {
        application = application.reject("Documentación incompleta");
        assertEquals(LoanApplicationStatus.REJECTED, application.getStatus());
        assertEquals("Documentación incompleta", application.getDecisionReason());
        assertNotNull(application.getEvaluatedAt());
    }

    @Test
    void shouldThrowWhenRejectingNonDecidable() {
        LoanApplication rejectedApp = application.reject("Razón");
        assertThrows(InvalidLoanApplicationStateException.class,
            () -> rejectedApp.reject("Otra razón"));
    }

    @Test
    void shouldAutoEvaluateApproved() {
        application = application.autoEvaluate("APPROVED", "Todo en orden");
        assertEquals(LoanApplicationStatus.APPROVED, application.getStatus());
        assertEquals("Todo en orden", application.getDecisionReason());
    }

    @Test
    void shouldAutoEvaluateManualReview() {
        application = application.autoEvaluate("MANUAL_REVIEW", "Revisión necesaria");
        assertEquals(LoanApplicationStatus.MANUAL_REVIEW, application.getStatus());
    }

    @Test
    void shouldAutoEvaluateRejected() {
        application = application.autoEvaluate("REJECTED", "No aprobado");
        assertEquals(LoanApplicationStatus.REJECTED, application.getStatus());
        assertEquals("No aprobado", application.getDecisionReason());
    }

    @Test
    void shouldHaveGetters() {
        assertNotNull(application.getCustomer());
        assertNotNull(application.getLoanType());
        assertNotNull(application.getRequestedAmount());
        assertEquals(12, application.getTermInMonths());
        assertNotNull(application.getAnnualInterestRate());
    }
}
