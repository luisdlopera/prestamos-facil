package com.prestamosfacil.application.customer;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.in.ProfileUseCase;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.user.models.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileUseCaseImpl implements ProfileUseCase {

    private final CustomerRepository customerRepository;

    public ProfileUseCaseImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional
    public Customer updateProfile(UUID customerId, String firstName, String lastName,
                                   BigDecimal baseSalary, String email) {
        Customer customer = customerRepository.findById(customerId)
                .or(() -> customerRepository.findByUserId(customerId))
                .orElseThrow(() -> new ApplicationException(Messages.AUTH_USER_NOT_FOUND));

        Customer.CustomerBuilder builder = Customer.builder()
                .id(customer.getId())
                .user(customer.getUser())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .documentNumber(customer.getDocumentNumber())
                .baseSalary(customer.getBaseSalary());

        if (firstName != null && !firstName.isBlank()) {
            builder.firstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            builder.lastName(lastName);
        }
        if (baseSalary != null) {
            if (baseSalary.compareTo(BigDecimal.ZERO) < 0 || baseSalary.compareTo(new BigDecimal("15000000.00")) > 0) {
                throw new ApplicationException(Messages.AUTH_SALARY_RANGE);
            }
            builder.baseSalary(new Money(baseSalary));
        }
        if (email != null && !email.isBlank() && !customer.getEmail().getValue().equals(email)) {
            EmailAddress newEmail = new EmailAddress(email);
            builder.email(newEmail);
            if (customer.getUser() != null) {
                builder.user(User.builder()
                        .id(customer.getUser().getId())
                        .firstName(customer.getUser().getFirstName())
                        .lastName(customer.getUser().getLastName())
                        .email(newEmail)
                        .passwordHash(customer.getUser().getPasswordHash())
                        .role(customer.getUser().getRole())
                        .failedLoginAttempts(customer.getUser().getFailedLoginAttempts())
                        .blockedUntil(customer.getUser().getBlockedUntil())
                        .lastLoginAt(customer.getUser().getLastLoginAt())
                        .enabled(customer.getUser().isEnabled())
                        .build());
            }
        }

        return customerRepository.save(builder.build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByUserId(UUID userId) {
        return customerRepository.findByUserId(userId);
    }
}
