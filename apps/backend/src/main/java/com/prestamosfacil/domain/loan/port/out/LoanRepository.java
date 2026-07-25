package com.prestamosfacil.domain.loan.port.out;

import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.shared.PageResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository {

    Optional<Loan> findById(UUID id);
    Optional<Loan> findByLoanApplicationId(UUID loanApplicationId);
    Loan save(Loan loan);
    PageResult<Loan> findAll(int page, int perPage, String sortBy, String sortDir);
    PageResult<Loan> findByFilters(UUID customerId, String search, int page, int perPage, String sortBy, String sortDir);

    record LoanStatistics(long totalCount, BigDecimal totalPrincipalAmount, BigDecimal averageInterestRate) {}
    LoanStatistics getLoanStatistics();
}
