package com.prestamosfacil.application.reporting;

import com.prestamosfacil.domain.reporting.port.in.ReportUseCase;
import com.prestamosfacil.domain.reporting.models.ApprovedLoanSummary;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportUseCaseImpl implements ReportUseCase {

    private final LoanRepository loanRepository;

    public ReportUseCaseImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public ApprovedLoanSummary getApprovedLoansSummary() {
        var stats = loanRepository.getLoanStatistics();
        return new ApprovedLoanSummary(stats.totalPrincipalAmount(), (int) stats.totalCount(), stats.averageInterestRate());
    }
}
