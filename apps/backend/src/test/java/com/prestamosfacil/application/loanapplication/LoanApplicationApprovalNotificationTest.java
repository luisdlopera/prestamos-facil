package com.prestamosfacil.application.loanapplication;

import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.loanapplication.enums.ManualDecision;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loanapplication.event.ApplicationApprovedEvent;
import com.prestamosfacil.domain.shared.port.out.EventPublisher;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.loanapplication.port.in.LoanApplicationUseCase;
import com.prestamosfacil.domain.loanapplication.port.out.AutomaticLoanEvaluationPort;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.customer.models.PhoneNumber;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanApplicationApprovalNotificationTest {

    private LoanApplicationRepository loanApplicationRepository;
    private CustomerRepository customerRepository;
    private LoanTypeRepository loanTypeRepository;
    private LoanUseCase loanUseCase;
    private PaymentPlanUseCase paymentPlanUseCase;
    private AutomaticLoanEvaluationPort evaluationPort;
    private EventPublisher eventPublisher;

    private LoanApplicationUseCase loanApplicationUseCase;

    private Customer testCustomer;
    private LoanType testLoanType;
    private LoanApplication testApplication;

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        customerRepository = mock(CustomerRepository.class);
        loanTypeRepository = mock(LoanTypeRepository.class);
        loanUseCase = mock(LoanUseCase.class);
        paymentPlanUseCase = mock(PaymentPlanUseCase.class);
        evaluationPort = mock(AutomaticLoanEvaluationPort.class);
        eventPublisher = mock(EventPublisher.class);

        loanApplicationUseCase = new LoanApplicationUseCaseImpl(
            loanApplicationRepository, customerRepository, loanTypeRepository,
            evaluationPort, loanUseCase, paymentPlanUseCase, eventPublisher);

        testCustomer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan.perez@example.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan.perez@example.com"))
                .passwordHash("hashedpass")
                .build())
            .build();

        testLoanType = new LoanType(
            "Préstamo Personal",
            new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")),
            new Money(new BigDecimal("50000000"))
        );

        testApplication = new LoanApplication(
            testCustomer,
            testLoanType,
            new Money(new BigDecimal("10000000")),
            12
        );
    }

    @Test
    @DisplayName("Al aprobar una solicitud en Pendiente de Revisión se debe notificar con el plan de pagos")
    void shouldNotifyWithPaymentPlanOnApproval() {
        UUID applicationId = testApplication.getId();
        UUID loanId = UUID.randomUUID();

        when(loanApplicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(i -> i.getArgument(0));

        Loan mockLoan = mock(Loan.class);
        when(mockLoan.getId()).thenReturn(loanId);
        when(loanUseCase.createLoanFromApplication(applicationId)).thenReturn(mockLoan);

        PaymentInstallment installment1 = new PaymentInstallment(
            loanId, 1, LocalDate.of(2026, 8, 1),
            new Money(new BigDecimal("10000000")),
            new Money(new BigDecimal("88848.79")),
            new Money(new BigDecimal("78848.79")),
            new Money(new BigDecimal("10000.00")),
            new Money(new BigDecimal("9921151.21"))
        );
        when(paymentPlanUseCase.getPaymentPlan(loanId)).thenReturn(List.of(installment1));

        LoanApplication approvedApp = loanApplicationUseCase.manualDecision(applicationId, ManualDecision.APPROVE, null);

        assertEquals(LoanApplicationStatus.APPROVED, approvedApp.getStatus());

        ArgumentCaptor<ApplicationApprovedEvent> captor = ArgumentCaptor.forClass(ApplicationApprovedEvent.class);
        verify(eventPublisher).publish(captor.capture());

        ApplicationApprovedEvent event = captor.getValue();
        assertNotNull(event);
        assertEquals("juan.perez@example.com", event.customerEmail());
        assertNotNull(event.installments());
        assertEquals(1, event.installments().size());
        assertEquals(1, event.installments().get(0).getInstallmentNumber());
        assertEquals(new Money(new BigDecimal("10000000")), event.installments().get(0).getOpeningBalance());
    }
}

