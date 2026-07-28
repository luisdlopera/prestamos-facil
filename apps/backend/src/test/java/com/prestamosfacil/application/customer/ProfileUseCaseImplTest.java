package com.prestamosfacil.application.customer;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileUseCaseImplTest {

    private CustomerRepository customerRepository;
    private ProfileUseCaseImpl profileUseCase;
    private Customer customer;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        profileUseCase = new ProfileUseCaseImpl(customerRepository);

        customerId = UUID.randomUUID();
        customer = Customer.builder()
            .id(customerId)
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("hash")
                .build())
            .build();
    }

    @Test
    void shouldFindById() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Optional<Customer> result = profileUseCase.findById(customerId);

        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getFirstName());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        Optional<Customer> result = profileUseCase.findById(customerId);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldUpdateFirstName() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, "Carlos", null, null, null);

        assertEquals("Carlos", updated.getFirstName());
        assertEquals("Perez", updated.getLastName());
        assertEquals("juan@test.com", updated.getEmail().getValue());
    }

    @Test
    void shouldUpdateLastName() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, null, "Gomez", null, null);

        assertEquals("Juan", updated.getFirstName());
        assertEquals("Gomez", updated.getLastName());
    }

    @Test
    void shouldUpdateBaseSalary() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, null, null, new BigDecimal("6000000"), null);

        assertEquals(0, new BigDecimal("6000000").compareTo(updated.getBaseSalary().getAmount()));
    }

    @Test
    void shouldUpdateEmail() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, null, null, null, "nuevo@test.com");

        assertEquals("nuevo@test.com", updated.getEmail().getValue());
    }

    @Test
    void shouldUpdateMultipleFields() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, "Carlos", "Gomez", new BigDecimal("7000000"), "carlos@test.com");

        assertEquals("Carlos", updated.getFirstName());
        assertEquals("Gomez", updated.getLastName());
        assertEquals(0, new BigDecimal("7000000").compareTo(updated.getBaseSalary().getAmount()));
        assertEquals("carlos@test.com", updated.getEmail().getValue());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () ->
            profileUseCase.updateProfile(customerId, "Carlos", null, null, null));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateEmailWhenSameValue() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, null, null, null, "juan@test.com");

        assertEquals("juan@test.com", updated.getEmail().getValue());
    }

    @Test
    void shouldIgnoreBlankFirstName() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer updated = profileUseCase.updateProfile(customerId, "  ", null, null, null);

        assertEquals("Juan", updated.getFirstName());
    }
}
