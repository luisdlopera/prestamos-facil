package com.prestamosfacil.application.shared;

import com.prestamosfacil.application.loan.LoanUseCaseImpl;
import com.prestamosfacil.application.loanapplication.LoanApplicationUseCaseImpl;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.models.LoanApplicationStatusEntry;
import com.prestamosfacil.domain.loanapplication.port.in.LoanApplicationUseCase;
import com.prestamosfacil.domain.loanapplication.port.out.AutomaticLoanEvaluationPort;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.port.out.EventPublisher;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.application.loan.LoanUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationIdorTest {

    private LoanApplicationUseCase loanApplicationUseCase;
    private LoanUseCase loanUseCase;
    private LoanApplicationRepository loanApplicationRepository;
    private LoanRepository loanRepository;
    private CustomerRepository customerRepository;

    private Customer customerA;
    private Customer customerB;
    private UUID customerAId;
    private UUID customerBId;
    private LoanApplication appOfA;
    private UUID appOfAId;
    private Loan loanOfA;
    private UUID loanOfAId;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        LoanTypeRepository loanTypeRepository = mock(LoanTypeRepository.class);
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        loanRepository = mock(LoanRepository.class);
        PaymentPlanUseCase paymentPlanUseCase = mock(PaymentPlanUseCase.class);
        AutomaticLoanEvaluationPort evaluationPort = mock(AutomaticLoanEvaluationPort.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        LoanType loanType = mock(LoanType.class);

        loanApplicationUseCase = new LoanApplicationUseCaseImpl(
            loanApplicationRepository, customerRepository, loanTypeRepository,
            evaluationPort, mock(com.prestamosfacil.domain.loan.port.in.LoanUseCase.class), paymentPlanUseCase, eventPublisher);

        loanUseCase = new LoanUseCaseImpl(
            loanRepository, loanApplicationRepository, paymentPlanUseCase,
            mock(com.prestamosfacil.domain.loan.models.LoanCalculator.class));

        customerAId = UUID.randomUUID();
        customerBId = UUID.randomUUID();
        customerA = Customer.builder()
            .id(customerAId)
            .firstName("Alice")
            .lastName("A")
            .email(new EmailAddress("alice@test.com"))
            .documentNumber(new DocumentNumber("CC", "11111"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .build();
        customerB = Customer.builder()
            .id(customerBId)
            .firstName("Bob")
            .lastName("B")
            .email(new EmailAddress("bob@test.com"))
            .documentNumber(new DocumentNumber("CC", "22222"))
            .baseSalary(new Money(new BigDecimal("3000000")))
            .build();

        LoanType personalLoan = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        appOfA = new LoanApplication(customerA, personalLoan,
            new Money(new BigDecimal("10000000")), 12);
        appOfAId = appOfA.getId();

        loanOfAId = UUID.randomUUID();
        loanOfA = Loan.builder()
            .id(loanOfAId)
            .customer(customerA)
            .principalAmount(new Money(new BigDecimal("10000000")))
            .build();
    }

    @Test
    void customerAShouldNotSeeCustomerBLoanApplication() {
        when(loanApplicationRepository.findById(appOfAId)).thenReturn(Optional.of(appOfA));

        Optional<LoanApplication> result = loanApplicationUseCase.findByIdForUser(appOfAId, customerBId);

        assertTrue(result.isEmpty(), "Customer B should not see Customer A's application");
    }

    @Test
    void customerShouldSeeOwnLoanApplication() {
        when(loanApplicationRepository.findById(appOfAId)).thenReturn(Optional.of(appOfA));

        Optional<LoanApplication> result = loanApplicationUseCase.findByIdForUser(appOfAId, customerAId);

        assertTrue(result.isPresent(), "Customer A should see own application");
    }

    @Test
    void customerAShouldNotSeeCustomerBLoan() {
        when(loanRepository.findById(loanOfAId)).thenReturn(Optional.of(loanOfA));

        Optional<Loan> result = loanUseCase.findByIdForUser(loanOfAId, customerBId);

        assertTrue(result.isEmpty(), "Customer B should not see Customer A's loan");
    }

    @Test
    void customerShouldSeeOwnLoan() {
        when(loanRepository.findById(loanOfAId)).thenReturn(Optional.of(loanOfA));

        Optional<Loan> result = loanUseCase.findByIdForUser(loanOfAId, customerAId);

        assertTrue(result.isPresent(), "Customer A should see own loan");
    }

    @Test
    void nonExistentApplicationReturnsEmpty() {
        UUID unknownId = UUID.randomUUID();
        when(loanApplicationRepository.findById(unknownId)).thenReturn(Optional.empty());

        Optional<LoanApplication> result = loanApplicationUseCase.findByIdForUser(unknownId, customerAId);

        assertTrue(result.isEmpty());
    }

    @Test
    void nonExistentLoanReturnsEmpty() {
        UUID unknownId = UUID.randomUUID();
        when(loanRepository.findById(unknownId)).thenReturn(Optional.empty());

        Optional<Loan> result = loanUseCase.findByIdForUser(unknownId, customerAId);

        assertTrue(result.isEmpty());
    }
}
