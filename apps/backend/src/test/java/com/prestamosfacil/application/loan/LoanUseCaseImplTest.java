package com.prestamosfacil.application.loan;

import com.prestamosfacil.domain.loan.models.LoanCalculator;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoanUseCaseImplTest {

    private LoanRepository loanRepository;
    private LoanApplicationRepository loanApplicationRepository;
    private PaymentPlanUseCase paymentPlanUseCase;
    private LoanCalculator loanCalculator;
    private LoanUseCaseImpl loanUseCase;

    private Customer customer;
    private LoanType loanType;
    private LoanApplication approvedApplication;

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        paymentPlanUseCase = mock(PaymentPlanUseCase.class);
        loanCalculator = mock(LoanCalculator.class);
        // LoanCalculator inyectado porque LoanUseCaseImpl lo necesita para calcular monthlyPayment
        loanUseCase = new LoanUseCaseImpl(loanRepository, loanApplicationRepository,
                                           paymentPlanUseCase, loanCalculator);

        customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("hash")
                .build())
            .build();

        loanType = new LoanType("Personal", new BigDecimal("12.0"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));

        approvedApplication = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        // approve() es parametreless — la tasa viene del snapshot de la solicitud
        approvedApplication = approvedApplication.approve();
    }

    @Test
    void shouldCreateLoanFromApprovedApplication() {
        UUID appId = approvedApplication.getId();
        when(loanApplicationRepository.findById(appId)).thenReturn(Optional.of(approvedApplication));
        when(loanRepository.findByLoanApplicationId(appId)).thenReturn(Optional.empty());
        when(loanCalculator.calculateMonthlyPayment(any(), any(), anyInt()))
            .thenReturn(new Money(new BigDecimal("888488.79")));
        when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Loan loan = loanUseCase.createLoanFromApplication(appId);

        assertNotNull(loan);
        assertEquals(approvedApplication, loan.getLoanApplication());
        verify(paymentPlanUseCase).generatePaymentPlan(loan.getId());
    }

    @Test
    void shouldThrowWhenApplicationNotFound() {
        UUID appId = UUID.randomUUID();
        when(loanApplicationRepository.findById(appId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> loanUseCase.createLoanFromApplication(appId));
    }

    @Test
    void shouldThrowWhenApplicationNotApproved() {
        LoanApplication pendingApp = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        UUID appId = pendingApp.getId();
        when(loanApplicationRepository.findById(appId)).thenReturn(Optional.of(pendingApp));

        assertThrows(IllegalStateException.class,
            () -> loanUseCase.createLoanFromApplication(appId));
    }

    @Test
    void shouldThrowWhenLoanAlreadyExists() {
        UUID appId = approvedApplication.getId();
        when(loanApplicationRepository.findById(appId)).thenReturn(Optional.of(approvedApplication));
        when(loanRepository.findByLoanApplicationId(appId)).thenReturn(Optional.of(mock(Loan.class)));

        assertThrows(IllegalStateException.class,
            () -> loanUseCase.createLoanFromApplication(appId));
    }

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Loan loan = mock(Loan.class);
        when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanUseCase.findById(id);
        assertTrue(result.isPresent());
    }

    @Test
    void shouldFindByLoanApplicationIdWhenNotFoundDirectly() {
        UUID appId = UUID.randomUUID();
        Loan loan = mock(Loan.class);
        when(loanRepository.findById(appId)).thenReturn(Optional.empty());
        when(loanRepository.findByLoanApplicationId(appId)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanUseCase.findById(appId);
        assertTrue(result.isPresent());
    }

    @Test
    void shouldFindAll() {
        PageResult<Loan> pageResult = new PageResult<>(List.of(), 0, 20, 0);
        when(loanRepository.findAll(0, 20, null, null)).thenReturn(pageResult);

        PageResult<Loan> result = loanUseCase.findAll(0, 20, null, null);
        assertEquals(0, result.getTotal());
    }

    @Test
    void shouldFindByFilters() {
        PageResult<Loan> pageResult = new PageResult<>(List.of(), 0, 20, 0);
        when(loanRepository.findByFilters(any(), any(), eq(0), eq(20), any(), any())).thenReturn(pageResult);

        PageResult<Loan> result = loanUseCase.findByFilters(UUID.randomUUID(), "search", 0, 20, null, null);
        assertEquals(0, result.getTotal());
    }
}
