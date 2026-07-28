package com.prestamosfacil.application.reporting;

import com.prestamosfacil.domain.reporting.models.ApprovedLoanSummary;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportUseCaseImplTest {

    private LoanRepository loanRepository;
    private ReportUseCaseImpl reportUseCase;

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        reportUseCase = new ReportUseCaseImpl(loanRepository);
    }

    @Test
    void shouldGetApprovedLoansSummary() {
        when(loanRepository.getLoanStatistics()).thenReturn(
            new LoanRepository.LoanStatistics(1, new BigDecimal("10000000"), new BigDecimal("12.0")));

        ApprovedLoanSummary summary = reportUseCase.getApprovedLoansSummary();
        assertEquals(0, new BigDecimal("10000000").compareTo(summary.totalApproved()));
        assertEquals(1, summary.activeLoans());
        assertNotNull(summary.averageRate());
    }

    @Test
    void shouldReturnEmptySummaryWhenNoLoans() {
        when(loanRepository.getLoanStatistics()).thenReturn(
            new LoanRepository.LoanStatistics(0, BigDecimal.ZERO, BigDecimal.ZERO));

        ApprovedLoanSummary summary = reportUseCase.getApprovedLoansSummary();
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.totalApproved()));
        assertEquals(0, summary.activeLoans());
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.averageRate()));
    }
}
