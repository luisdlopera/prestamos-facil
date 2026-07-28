package com.prestamosfacil.infrastructure.adapter.out.customer;

import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica que el lookup usado por JwtAuthFilter no devuelve una asociación User lazy. */
@SpringBootTest
@ActiveProfiles("test")
class CustomerRepositorySecurityIntegrationTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserAuthUseCase userAuthUseCase;

    @Test
    void findByUserIdInitializesUserDataOutsideRepositorySession() {
        UUID userId = userAuthUseCase.registerCustomer("Lookup", "Customer", "lookup@test.com",
            "CC", "5656565656", new java.math.BigDecimal("2000000"), "LookupPass1@").aggregateId();

        var customer = customerRepository.findByUserId(userId);
        assertThat(customer).isPresent();
        assertThat(customer.orElseThrow().getUser().getEmail().getValue()).isEqualTo("lookup@test.com");
    }
}
