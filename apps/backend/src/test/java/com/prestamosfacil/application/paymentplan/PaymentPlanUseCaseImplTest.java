package com.prestamosfacil.application.paymentplan;

import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loan.utils.PaymentPlanCalculator;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import com.prestamosfacil.domain.paymentplan.port.out.PaymentInstallmentRepository;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentPlanUseCaseImplTest {

    private LoanRepository loanRepository;
    private PaymentInstallmentRepository installmentRepository;
    private PaymentPlanCalculator paymentPlanCalculator;
    private PaymentPlanUseCaseImpl paymentPlanUseCase;

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        installmentRepository = mock(PaymentInstallmentRepository.class);
        paymentPlanCalculator = mock(PaymentPlanCalculator.class);
        paymentPlanUseCase = new PaymentPlanUseCaseImpl(loanRepository, installmentRepository, paymentPlanCalculator);
    }

    @Test
    void shouldGeneratePaymentPlan() {
        UUID loanId = UUID.randomUUID();
        Loan loan = mock(Loan.class);
        when(loan.getId()).thenReturn(loanId);
        when(loan.getPrincipalAmount()).thenReturn(new com.prestamosfacil.domain.shared.Money(java.math.BigDecimal.TEN));
        when(loan.getAnnualInterestRate()).thenReturn(java.math.BigDecimal.TEN);
        when(loan.getTermInMonths()).thenReturn(12);
        when(loan.calculateFirstDueDate()).thenReturn(java.time.LocalDate.now());
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        List<PaymentInstallment> installments = List.of(mock(PaymentInstallment.class));
        when(paymentPlanCalculator.calculateInstallments(any(), any(), any(), anyInt(), any()))
            .thenReturn(installments);
        when(installmentRepository.saveAll(installments)).thenReturn(installments);

        List<PaymentInstallment> result = paymentPlanUseCase.generatePaymentPlan(loanId);
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowWhenLoanNotFoundForGenerate() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> paymentPlanUseCase.generatePaymentPlan(loanId));
    }

    @Test
    void shouldGetPaymentPlanByLoanId() {
        UUID loanId = UUID.randomUUID();
        PaymentInstallment installment = mock(PaymentInstallment.class);
        when(installmentRepository.findByLoanId(loanId)).thenReturn(List.of(installment));

        List<PaymentInstallment> result = paymentPlanUseCase.getPaymentPlan(loanId);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetPaymentPlanByApplicationIdWhenNoInstallments() {
        UUID loanId = UUID.randomUUID();
        when(installmentRepository.findByLoanId(loanId)).thenReturn(List.of());

        Loan loan = mock(Loan.class);
        when(loan.getId()).thenReturn(loanId);
        when(loanRepository.findByLoanApplicationId(loanId)).thenReturn(Optional.of(loan));

        PaymentInstallment installment = mock(PaymentInstallment.class);
        when(installmentRepository.findByLoanId(loan.getId())).thenReturn(List.of(installment));

        List<PaymentInstallment> result = paymentPlanUseCase.getPaymentPlan(loanId);
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNotFound() {
        UUID loanId = UUID.randomUUID();
        when(installmentRepository.findByLoanId(loanId)).thenReturn(List.of());
        when(loanRepository.findByLoanApplicationId(loanId)).thenReturn(Optional.empty());

        List<PaymentInstallment> result = paymentPlanUseCase.getPaymentPlan(loanId);
        assertTrue(result.isEmpty());
    }
}
