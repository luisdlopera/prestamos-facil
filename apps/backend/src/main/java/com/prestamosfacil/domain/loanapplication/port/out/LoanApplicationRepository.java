package com.prestamosfacil.domain.loanapplication.port.out;

import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.shared.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationRepository {

    Optional<LoanApplication> findById(UUID id);
    Optional<LoanApplication> findByIdAndCustomerId(UUID id, UUID customerId);
    List<LoanApplication> findAllById(Collection<UUID> ids);
    LoanApplication save(LoanApplication application);
    PageResult<LoanApplication> findAll(int page, int perPage, String sortBy, String sortDir);
    PageResult<LoanApplication> findByFilters(UUID customerId, String statusCode, String search, int page, int perPage, String sortBy, String sortDir);
}
