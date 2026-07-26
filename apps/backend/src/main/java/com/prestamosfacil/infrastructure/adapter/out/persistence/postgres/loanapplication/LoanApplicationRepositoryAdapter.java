package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.entity.LoanTypeEntity;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.models.LoanApplicationStatusEntry;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity.LoanApplicationEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity.LoanApplicationStatusEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity.LoanApplicationStatusHistoryEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.repository.LoanApplicationJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.repository.LoanApplicationStatusHistoryJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.SearchSpecUtils;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.SortUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Set;

@Component
public class LoanApplicationRepositoryAdapter implements LoanApplicationRepository {
    private final LoanApplicationJpaRepository jpaRepository;
    private final LoanApplicationStatusHistoryJpaRepository historyJpaRepository;
    private final CustomerRepository customerRepository;
    private final LoanTypeRepository loanTypeRepository;

    public LoanApplicationRepositoryAdapter(LoanApplicationJpaRepository jpa, LoanApplicationStatusHistoryJpaRepository history,
                                             CustomerRepository cr, LoanTypeRepository ltr) {
        this.jpaRepository = jpa; this.historyJpaRepository = history; this.customerRepository = cr; this.loanTypeRepository = ltr;
    }

    @Override public Optional<LoanApplication> findById(UUID id) { return jpaRepository.findById(id).flatMap(this::toDomain); }

    @Override public LoanApplication save(LoanApplication application) {
        LoanApplicationEntity entity = jpaRepository.findById(application.getId()).map(e -> toEntity(application, e)).orElseGet(() -> toEntity(application, new LoanApplicationEntity()));
        LoanApplicationEntity saved = jpaRepository.save(entity);
        saveStatusHistory(application);
        return toDomain(saved).orElseThrow(() -> new RuntimeException("Failed to map saved loan application entity to domain"));
    }

    @Override public List<LoanApplication> findAllById(Collection<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return toDomainBatch(jpaRepository.findAllById(ids)).stream().flatMap(Optional::stream).toList();
    }

    private void saveStatusHistory(LoanApplication application) {
        LoanApplicationStatusEntry entry = application.getCurrentStatusEntry();
        if (entry == null) return;
        var opt = historyJpaRepository.findByLoanApplicationIdAndClosedAtIsNull(application.getId());
        if (opt.isPresent()) {
            var active = opt.get();
            if (!active.getStatus().getCode().equals(entry.getStatus().name()) || (entry.getId() != null && !entry.getId().equals(active.getId()))) {
                active.setClosedAt(Instant.now()); historyJpaRepository.save(active);
                createHistoryEntity(application.getId(), entry);
            } else {
                active.setDecisionReason(entry.getDecisionReason()); active.setEvaluatedBy(entry.getEvaluatedBy());
                historyJpaRepository.save(active);
            }
        } else createHistoryEntity(application.getId(), entry);
    }

    private void createHistoryEntity(UUID appId, LoanApplicationStatusEntry entry) {
        var e = new LoanApplicationStatusHistoryEntity();
        e.setId(entry.getId() != null ? entry.getId() : UUID.randomUUID());
        e.setLoanApplicationId(appId); e.setStatus(LoanApplicationStatusEntity.fromCode(entry.getStatus().name()));
        e.setDecisionReason(entry.getDecisionReason()); e.setEvaluatedBy(entry.getEvaluatedBy());
        e.setOpenedAt(entry.getOpenedAt() != null ? entry.getOpenedAt() : Instant.now()); e.setClosedAt(null);
        historyJpaRepository.save(e);
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("requestedAmount", "termInMonths", "annualInterestRate", "createdAt");

    @Override public PageResult<LoanApplication> findAll(int page, int perPage, String sortBy, String sortDir) {
        var sp = jpaRepository.findAll(PageRequest.of(page, perPage, SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir)));
        var content = toDomainBatch(sp.getContent()).stream().flatMap(Optional::stream).toList();
        return new PageResult<>(content, page, perPage, sp.getTotalElements());
    }

    @Override public PageResult<LoanApplication> findByFilters(UUID customerId, String statusCode, String search, int page, int perPage, String sortBy, String sortDir) {
        Specification<LoanApplicationEntity> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (customerId != null) predicates.add(cb.equal(root.get("customerId"), customerId));
            if (statusCode != null && !statusCode.isBlank()) {
                var sub = query.subquery(UUID.class);
                var hr = sub.from(LoanApplicationStatusHistoryEntity.class);
                sub.select(hr.get("loanApplicationId")).where(cb.and(cb.isNull(hr.get("closedAt")), cb.equal(hr.get("status").get("code"), statusCode)));
                predicates.add(root.get("id").in(sub));
            }
            if (search != null && !search.isBlank()) {
                String p = "%" + search.trim().toLowerCase() + "%";
                var cs = SearchSpecUtils.buildCustomerSearchSubquery(query, cb, p);
                var ls = query.subquery(UUID.class); var lr = ls.from(LoanTypeEntity.class);
                ls.select(lr.get("id")).where(cb.like(cb.lower(lr.get("name")), p));
                predicates.add(cb.or(root.get("customerId").in(cs), root.get("loanTypeId").in(ls)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        var sp = jpaRepository.findAll(spec, PageRequest.of(page, perPage, SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir)));
        var content = toDomainBatch(sp.getContent()).stream().flatMap(Optional::stream).toList();
        return new PageResult<>(content, page, perPage, sp.getTotalElements());
    }

    private List<Optional<LoanApplication>> toDomainBatch(List<LoanApplicationEntity> entities) {
        if (entities.isEmpty()) return List.of();
        var customerIds = entities.stream().map(LoanApplicationEntity::getCustomerId).collect(Collectors.toSet());
        var loanTypeIds = entities.stream().map(LoanApplicationEntity::getLoanTypeId).collect(Collectors.toSet());
        var customers = customerRepository.findAllById(customerIds).stream().collect(Collectors.toMap(Customer::getId, c -> c));
        var loanTypes = loanTypeRepository.findAllById(loanTypeIds).stream().collect(Collectors.toMap(LoanType::getId, lt -> lt));
        var appIds = entities.stream().map(LoanApplicationEntity::getId).collect(Collectors.toSet());
        var history = historyJpaRepository.findByLoanApplicationIdInAndClosedAtIsNull(appIds).stream().collect(Collectors.toMap(LoanApplicationStatusHistoryEntity::getLoanApplicationId, h -> h));
        return entities.stream().map(e -> toDomainWithLookups(e, customers, loanTypes, history)).toList();
    }

    private Optional<LoanApplication> toDomainWithLookups(LoanApplicationEntity e, Map<UUID, Customer> customers, Map<UUID, LoanType> loanTypes, Map<UUID, LoanApplicationStatusHistoryEntity> history) {
        Customer c = Optional.ofNullable(customers.get(e.getCustomerId())).orElse(null);
        LoanType lt = Optional.ofNullable(loanTypes.get(e.getLoanTypeId())).orElse(null);
        if (c == null || lt == null) return Optional.empty();
        LoanApplicationStatusEntry entry = Optional.ofNullable(history.get(e.getId())).map(h -> LoanApplicationStatusEntry.builder().id(h.getId()).loanApplicationId(h.getLoanApplicationId()).status(LoanApplicationStatus.valueOf(h.getStatus().getCode())).decisionReason(h.getDecisionReason()).evaluatedBy(h.getEvaluatedBy()).openedAt(h.getOpenedAt()).closedAt(null).build()).orElseGet(() -> LoanApplicationStatusEntry.initialEntry(e.getId()));
        return Optional.of(LoanApplication.builder().id(e.getId()).customer(c).loanType(lt).requestedAmount(new Money(e.getRequestedAmount())).termInMonths(e.getTermInMonths()).annualInterestRate(e.getAnnualInterestRate()).currentStatusEntry(entry).createdAt(e.getCreatedAt()).build());
    }

    private Optional<LoanApplication> toDomain(LoanApplicationEntity entity) {
        Optional<Customer> c = customerRepository.findById(entity.getCustomerId());
        Optional<LoanType> lt = loanTypeRepository.findById(entity.getLoanTypeId());
        if (c.isEmpty() || lt.isEmpty()) return Optional.empty();
        LoanApplicationStatusEntry entry = historyJpaRepository.findByLoanApplicationIdAndClosedAtIsNull(entity.getId()).map(h -> LoanApplicationStatusEntry.builder().id(h.getId()).loanApplicationId(h.getLoanApplicationId()).status(LoanApplicationStatus.valueOf(h.getStatus().getCode())).decisionReason(h.getDecisionReason()).evaluatedBy(h.getEvaluatedBy()).openedAt(h.getOpenedAt()).closedAt(null).build()).orElseGet(() -> LoanApplicationStatusEntry.initialEntry(entity.getId()));
        return Optional.of(LoanApplication.builder().id(entity.getId()).customer(c.get()).loanType(lt.get()).requestedAmount(new Money(entity.getRequestedAmount())).termInMonths(entity.getTermInMonths()).annualInterestRate(entity.getAnnualInterestRate()).currentStatusEntry(entry).createdAt(entity.getCreatedAt()).build());
    }

    private LoanApplicationEntity toEntity(LoanApplication domain, LoanApplicationEntity entity) {
        entity.setId(domain.getId()); entity.setCustomerId(domain.getCustomer().getId()); entity.setLoanTypeId(domain.getLoanType().getId());
        entity.setRequestedAmount(domain.getRequestedAmount().getAmount()); entity.setTermInMonths(domain.getTermInMonths());
        entity.setAnnualInterestRate(domain.getAnnualInterestRate()); return entity;
    }
}
