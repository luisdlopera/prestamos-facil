package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.reporting;

import com.prestamosfacil.domain.reporting.models.ApprovedLoansTotal;
import com.prestamosfacil.domain.reporting.port.out.ApprovedLoanReportPort;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.repository.LoanJpaRepository;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class ApprovedLoanReportAdapter implements ApprovedLoanReportPort {

    private final LoanJpaRepository loanJpaRepository;

    public ApprovedLoanReportAdapter(LoanJpaRepository loanJpaRepository) {
        this.loanJpaRepository = loanJpaRepository;
    }

    @Override
    public ApprovedLoansTotal getTotalApproved() {
        List<Object[]> rows = loanJpaRepository.getTotalApprovedAggregation();
        if (rows.isEmpty()) {
            return new ApprovedLoansTotal(BigDecimal.ZERO, 0, Instant.now());
        }
        Object[] result = rows.get(0);
        long count = ((Number) result[0]).longValue();
        Object amountObj = result[1];
        BigDecimal totalAmount = amountObj == null ? BigDecimal.ZERO :
            (amountObj instanceof BigDecimal bd ? bd : new BigDecimal(amountObj.toString()));
        return new ApprovedLoansTotal(totalAmount, count, Instant.now());
    }
}
