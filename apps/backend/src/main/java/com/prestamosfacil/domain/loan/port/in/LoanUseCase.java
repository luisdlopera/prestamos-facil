package com.prestamosfacil.domain.loan.port.in;

import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface LoanUseCase {

    Loan createLoanFromApplication(UUID loanApplicationId);

    Optional<Loan> findById(UUID id);
    Optional<Loan> findByIdForUser(UUID loanId, UUID customerId);
    PageResult<Loan> findAll(int page, int perPage, String sortBy, String sortDir);
    PageResult<Loan> findByFilters(UUID customerId, String search, int page, int perPage, String sortBy, String sortDir);
}
