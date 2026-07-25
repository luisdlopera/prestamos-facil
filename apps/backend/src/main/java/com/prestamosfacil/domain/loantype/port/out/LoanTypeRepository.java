package com.prestamosfacil.domain.loantype.port.out;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.shared.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanTypeRepository {

    Optional<LoanType> findById(UUID id);

    List<LoanType> findAllActive();

    PageResult<LoanType> findAllAdmin(String search, Boolean active, int page, int size, String sortBy, String sortDir);

    List<LoanType> findAllById(Collection<UUID> ids);

    boolean existsByName(String name, UUID excludeId);

    boolean hasRelatedRecords(UUID id);

    LoanType save(LoanType loanType);

    void saveAll(List<LoanType> loanTypes);

    void deleteById(UUID id);
}
