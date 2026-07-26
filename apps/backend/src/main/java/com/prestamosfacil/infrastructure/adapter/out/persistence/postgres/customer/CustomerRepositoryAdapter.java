package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.UserJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity.CustomerEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.repository.CustomerJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.repository.DocumentTypeJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.CustomerEntityMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared.SortUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository repository;
    private final UserJpaRepository userRepository;
    private final DocumentTypeJpaRepository documentTypeRepository;

    public CustomerRepositoryAdapter(CustomerJpaRepository repository, UserJpaRepository userRepository,
                                     DocumentTypeJpaRepository documentTypeRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.documentTypeRepository = documentTypeRepository;
    }
    @Override public Optional<Customer> findById(UUID id) { return repository.findById(id).map(CustomerEntityMapper::toCustomer); }
    @Override public Optional<Customer> findByEmail(String email) { return repository.findByEmail(email).map(CustomerEntityMapper::toCustomer); }
    @Override public Optional<Customer> findByUserId(UUID userId) { return repository.findByUserId(userId).map(CustomerEntityMapper::toCustomer); }
    @Override public List<Customer> findAllById(Collection<UUID> ids) { return repository.findAllById(ids).stream().map(CustomerEntityMapper::toCustomer).toList(); }
    @Override public boolean existsByEmail(String email) { return repository.findByEmail(email).isPresent(); }
    @Override public boolean existsByDocumentNumber(String value) { return repository.existsByDocumentNumber(value); }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("firstName", "lastName", "baseSalary", "createdAt");
    @Override public PageResult<Customer> findAll(int page, int perPage, String sortBy, String sortDir) {
        var result = repository.findAllCustomers(PageRequest.of(page, perPage, SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir)));
        return new PageResult<>(result.getContent().stream().map(CustomerEntityMapper::toCustomer).toList(), page, perPage, result.getTotalElements());
    }
    @Override public PageResult<Customer> findBySearch(String search, int page, int perPage, String sortBy, String sortDir) {
        var result = repository.findBySearch(search, PageRequest.of(page, perPage, SortUtils.buildSort(ALLOWED_SORT_FIELDS, sortBy, sortDir)));
        return new PageResult<>(result.getContent().stream().map(CustomerEntityMapper::toCustomer).toList(), page, perPage, result.getTotalElements());
    }

    @Override public Customer save(Customer customer) {
        UUID userId = customer.getUser() != null ? customer.getUser().getId() : customer.getId();
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (customer.getUser() != null) {
            if (customer.getUser().getEmail() != null) user.setEmail(customer.getUser().getEmail().getValue());
            if (customer.getUser().getPasswordHash() != null) user.setPasswordHash(customer.getUser().getPasswordHash());
            user.setFailedLoginAttempts(customer.getUser().getFailedLoginAttempts());
            user.setBlockedUntil(customer.getUser().getBlockedUntil());
            user.setLastLoginAt(customer.getUser().getLastLoginAt());
            user.setEnabled(customer.getUser().isEnabled());
            userRepository.save(user);
        }
        CustomerEntity entity = repository.findById(customer.getId()).orElseGet(CustomerEntity::new);
        entity.setId(customer.getId()); entity.setUser(user);
        entity.setFirstName(customer.getFirstName()); entity.setLastName(customer.getLastName());
        entity.setDocumentType(documentTypeRepository.getReferenceById(customer.getDocumentNumber().getType()));
        entity.setDocumentNumber(customer.getDocumentNumber().getNumber());
        entity.setBaseSalary(customer.getBaseSalary().getAmount());
        return CustomerEntityMapper.toCustomer(repository.save(entity));
    }
}
