package com.prestamosfacil.domain.loantype.port.in;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.shared.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanTypeUseCase {

    List<LoanType> findAllActive();

    PageResult<LoanType> findAllAdmin(String search, Boolean active, int page, int size, String sortBy, String sortDir);

    Optional<LoanType> findById(UUID id);

    LoanType create(LoanType loanType);

    LoanType update(UUID id, LoanType loanType);

    LoanType toggleStatus(UUID id, boolean active);

    void reorder(List<UUID> orderedIds);

    void delete(UUID id);
}
