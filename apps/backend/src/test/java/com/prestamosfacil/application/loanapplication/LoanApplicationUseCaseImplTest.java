package com.prestamosfacil.application.loanapplication;

import com.prestamosfacil.domain.loanapplication.event.ApplicationReceivedEvent;
import com.prestamosfacil.domain.shared.port.out.EventPublisher;
import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.loanapplication.enums.ManualDecision;
import com.prestamosfacil.domain.loanapplication.exceptions.InvalidLoanApplicationStateException;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loanapplication.port.in.LoanApplicationUseCase;
import com.prestamosfacil.domain.loanapplication.port.out.AutomaticLoanEvaluationPort;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanApplicationUseCaseImplTest {

    private LoanApplicationRepository loanApplicationRepository;
    private CustomerRepository customerRepository;
    private LoanTypeRepository loanTypeRepository;
    private LoanUseCase loanUseCase;
    private PaymentPlanUseCase paymentPlanUseCase;
    private AutomaticLoanEvaluationPort evaluationPort;
    private EventPublisher eventPublisher;
    private LoanApplicationUseCase loanApplicationUseCase;

    private Customer customer;
    private LoanType loanType;

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

        customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("hash")
                .build())
            .build();

        loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
    }

    @Test
    void shouldCreateApplication() {
        UUID customerId = customer.getId();
        UUID loanTypeId = loanType.getId();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(loanTypeRepository.findById(loanTypeId)).thenReturn(Optional.of(loanType));
        when(loanApplicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanApplication result = loanApplicationUseCase.createApplication(
            customerId, loanTypeId, new BigDecimal("10000000"), 12);

        assertEquals(LoanApplicationStatus.PENDING_REVIEW, result.getStatus());
        assertEquals(new BigDecimal("12.0"), result.getAnnualInterestRate());
        verify(eventPublisher).publish(any(ApplicationReceivedEvent.class));
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID loanTypeId = loanType.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> loanApplicationUseCase.createApplication(customerId, loanTypeId,
                new BigDecimal("10000000"), 12));
    }

    @Test
    void shouldThrowWhenLoanTypeNotFound() {
        UUID customerId = customer.getId();
        UUID loanTypeId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(loanTypeRepository.findById(loanTypeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> loanApplicationUseCase.createApplication(customerId, loanTypeId,
                new BigDecimal("10000000"), 12));
    }
}
