package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.entity.LoanTypeEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.mapper.LoanTypeMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.repository.LoanTypeJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.SortUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.Set;

@Component
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {
    private final LoanTypeJpaRepository jpaRepository;
    private final LoanTypeMapper mapper;
    public LoanTypeRepositoryAdapter(LoanTypeJpaRepository jpaRepository, LoanTypeMapper mapper) {
        this.jpaRepository = jpaRepository; this.mapper = mapper;
    }

    @Override public Optional<LoanType> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    @Override public List<LoanType> findAllById(Collection<UUID> ids) { return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList(); }
    @Override public List<LoanType> findAllActive() { return jpaRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream().map(mapper::toDomain).toList(); }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "displayOrder", "interestRate", "minAmount", "maxAmount", "active", "createdAt");

    private static final Sort DEFAULT_LOAN_TYPE_SORT = Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("name"));

    @Override public PageResult<LoanType> findAllAdmin(String search, Boolean active, int page, int size, String sortBy, String sortDir) {
        Sort sort = SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir, DEFAULT_LOAN_TYPE_SORT);
        PageRequest pr = PageRequest.of(page, size, sort);
        String clean = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<LoanTypeEntity> ep;
        if (clean != null && active != null) ep = jpaRepository.findByNameContainingIgnoreCaseAndActiveOrDescriptionContainingIgnoreCaseAndActive(clean, active, clean, active, pr);
        else if (clean != null) ep = jpaRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(clean, clean, pr);
        else if (active != null) ep = jpaRepository.findByActive(active, pr);
        else ep = jpaRepository.findAll(pr);
        return new PageResult<>(ep.getContent().stream().map(mapper::toDomain).toList(), page, size, ep.getTotalElements());
    }

    @Override public boolean existsByName(String name, UUID excludeId) {
        if (name == null || name.isBlank()) return false;
        String t = name.trim();
        return excludeId != null ? jpaRepository.existsByNameIgnoreCaseAndIdNot(t, excludeId) : jpaRepository.existsByNameIgnoreCase(t);
    }
    @Override public boolean hasRelatedRecords(UUID id) { return id != null && jpaRepository.countRelatedApplications(id) > 0; }
    @Override public LoanType save(LoanType loanType) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(loanType))); }
    @Override public void saveAll(List<LoanType> loanTypes) { if (loanTypes != null && !loanTypes.isEmpty()) jpaRepository.saveAll(loanTypes.stream().map(mapper::toEntity).toList()); }
    @Override public void deleteById(UUID id) { jpaRepository.deleteById(id); }
}
