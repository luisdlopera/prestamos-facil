package com.prestamosfacil.domain.customer.port.in;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.shared.PageResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CustomerUseCase {

    Customer createCustomer(String firstName, String lastName, String email,
                            String documentType, String documentNumber,
                            BigDecimal baseSalary);

    Optional<Customer> findById(UUID id);
    Optional<Customer> findByUserId(UUID userId);
    PageResult<Customer> findAll(int page, int perPage, String sortBy, String sortDir);
    PageResult<Customer> findBySearch(String search, int page, int perPage, String sortBy, String sortDir);
}
