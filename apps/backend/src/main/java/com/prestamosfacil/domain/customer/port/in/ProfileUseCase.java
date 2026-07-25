package com.prestamosfacil.domain.customer.port.in;

import com.prestamosfacil.domain.customer.models.Customer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ProfileUseCase {
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByUserId(UUID userId);
    Customer updateProfile(UUID customerId, String firstName, String lastName, BigDecimal baseSalary, String email);
}
