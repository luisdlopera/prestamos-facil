package com.prestamosfacil.infrastructure.adapter.in.rest.shared;

import com.prestamosfacil.domain.customer.port.in.CustomerUseCase;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerContextResolver {

    private final CustomerUseCase customerUseCase;

    public CustomerContextResolver(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    public UUID resolveCustomerId(AuthPrincipal principal) {
        return customerUseCase.findByUserId(principal.userId())
                .orElseThrow(() -> new NotFoundException(Messages.CUSTOMER_NOT_FOUND.getValue()))
                .getId();
    }

    public void validateCustomerExists(UUID customerId) {
        customerUseCase.findById(customerId)
                .orElseThrow(() -> new NotFoundException(Messages.CUSTOMER_NOT_FOUND_ID.format(customerId)));
    }
}
