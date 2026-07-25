package com.prestamosfacil.domain.customer.port.out;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.shared.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Optional<Customer> findById(UUID id);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUserId(UUID userId);
    Customer save(Customer customer);
    boolean existsByEmail(String email);
    boolean existsByDocumentNumber(String documentNumber);
    List<Customer> findAllById(Collection<UUID> ids);
    PageResult<Customer> findAll(int page, int perPage, String sortBy, String sortDir);
    PageResult<Customer> findBySearch(String search, int page, int perPage, String sortBy, String sortDir);
}
