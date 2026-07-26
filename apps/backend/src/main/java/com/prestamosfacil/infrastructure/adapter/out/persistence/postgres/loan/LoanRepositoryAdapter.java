package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity.LoanEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.repository.LoanJpaRepository;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loan.enums.LoanStatus;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.SearchSpecUtils;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.SortUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LoanRepositoryAdapter implements LoanRepository {
    private final LoanJpaRepository jpaRepository;
    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    public LoanRepositoryAdapter(LoanJpaRepository jpaRepository, CustomerRepository customerRepository, LoanApplicationRepository loanApplicationRepository) {
        this.jpaRepository = jpaRepository; this.customerRepository = customerRepository; this.loanApplicationRepository = loanApplicationRepository;
    }

    @Override public Optional<Loan> findById(UUID id) { return jpaRepository.findById(id).flatMap(this::toDomain); }
    @Override public Optional<Loan> findByLoanApplicationId(UUID id) { return jpaRepository.findByLoanApplicationId(id).flatMap(this::toDomain); }

    @Override public Loan save(Loan loan) {
        LoanEntity entity = jpaRepository.findById(loan.getId())
            .map(e -> populateEntity(loan, e))
            .orElseGet(() -> populateEntity(loan, new LoanEntity()));
        return toDomain(jpaRepository.save(entity)).orElseThrow(() -> new RuntimeException("Failed to map saved loan entity to domain"));
    }

    @Override public LoanStatistics getLoanStatistics() {
        Object[] r = jpaRepository.getLoanStatisticsAggregation().get(0);
        return new LoanStatistics(((Number) r[0]).longValue(), (BigDecimal) r[1], (BigDecimal) r[2]);
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("principalAmount", "annualInterestRate", "termInMonths", "monthlyPayment", "approvedAt", "createdAt");

    @Override public PageResult<Loan> findAll(int page, int perPage, String sortBy, String sortDir) {
        var sp = jpaRepository.findAll(PageRequest.of(page, perPage, SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir)));
        var content = toDomainBatch(sp.getContent());
        return new PageResult<>(content, page, perPage, sp.getTotalElements());
    }

    @Override public PageResult<Loan> findByFilters(UUID customerId, String search, int page, int perPage, String sortBy, String sortDir) {
        org.springframework.data.jpa.domain.Specification<LoanEntity> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (customerId != null) predicates.add(cb.equal(root.get("customerId"), customerId));
            if (search != null && !search.isBlank()) {
                String p = "%" + search.trim().toLowerCase() + "%";
                var customerSub = SearchSpecUtils.buildCustomerSearchSubquery(query, cb, p);
                var loanPredicates = new ArrayList<Predicate>();
                loanPredicates.add(cb.like(cb.lower(cb.toString(root.get("id"))), p));
                try { loanPredicates.add(cb.equal(root.get("principalAmount"), new BigDecimal(search.trim()))); } catch (NumberFormatException ignored) {}
                try { loanPredicates.add(cb.equal(root.get("termInMonths"), Integer.parseInt(search.trim()))); } catch (NumberFormatException ignored) {}
                predicates.add(cb.or(root.get("customerId").in(customerSub), cb.or(loanPredicates.toArray(new Predicate[0]))));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var sp = jpaRepository.findAll(spec, PageRequest.of(page, perPage, SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir)));
        var content = toDomainBatch(sp.getContent());
        return new PageResult<>(content, page, perPage, sp.getTotalElements());
    }

    private List<Loan> toDomainBatch(List<LoanEntity> entities) {
        if (entities.isEmpty()) return List.of();
        var loanApplicationIds = entities.stream().map(LoanEntity::getLoanApplicationId).collect(Collectors.toSet());
        var customerIds = entities.stream().map(LoanEntity::getCustomerId).collect(Collectors.toSet());
        var loanApplications = loanApplicationRepository.findAllById(loanApplicationIds).stream()
            .collect(Collectors.toMap(LoanApplication::getId, la -> la));
        var customers = customerRepository.findAllById(customerIds).stream()
            .collect(Collectors.toMap(Customer::getId, c -> c));
        return entities.stream()
            .map(e -> toDomainWithLookups(e, loanApplications, customers))
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<Loan> toDomainWithLookups(LoanEntity entity, Map<UUID, LoanApplication> loanApplications, Map<UUID, Customer> customers) {
        LoanApplication la = loanApplications.get(entity.getLoanApplicationId());
        Customer c = customers.get(entity.getCustomerId());
        if (la == null || c == null) return Optional.empty();
        return Optional.of(buildLoan(entity, la, c));
    }

    private Optional<Loan> toDomain(LoanEntity entity) {
        Optional<LoanApplication> la = loanApplicationRepository.findById(entity.getLoanApplicationId());
        Optional<Customer> c = customerRepository.findById(entity.getCustomerId());
        if (la.isEmpty() || c.isEmpty()) return Optional.empty();
        return Optional.of(buildLoan(entity, la.get(), c.get()));
    }

    private Loan buildLoan(LoanEntity entity, LoanApplication la, Customer c) {
        return Loan.builder().id(entity.getId()).loanApplication(la).customer(c)
            .principalAmount(new Money(entity.getPrincipalAmount()))
            .annualInterestRate(entity.getAnnualInterestRate())
            .termInMonths(entity.getTermInMonths())
            .monthlyPayment(new Money(entity.getMonthlyPayment()))
            .status(entity.getStatus() == null ? LoanStatus.APPROVED : LoanStatus.valueOf(entity.getStatus()))
            .approvedAt(entity.getApprovedAt()).build();
    }

    private LoanEntity populateEntity(Loan domain, LoanEntity e) {
        e.setId(domain.getId()); e.setLoanApplicationId(domain.getLoanApplication().getId()); e.setCustomerId(domain.getCustomer().getId());
        e.setPrincipalAmount(domain.getPrincipalAmount().getAmount()); e.setAnnualInterestRate(domain.getAnnualInterestRate());
        e.setTermInMonths(domain.getTermInMonths()); e.setMonthlyPayment(domain.getMonthlyPayment().getAmount());
        e.setStatus(domain.getStatus().name()); e.setApprovedAt(domain.getApprovedAt());
        return e;
    }
}
