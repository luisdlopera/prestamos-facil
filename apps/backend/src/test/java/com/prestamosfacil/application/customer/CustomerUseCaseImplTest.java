package com.prestamosfacil.application.customer;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerUseCaseImplTest {

    private CustomerRepository customerRepository;
    private CustomerUseCaseImpl customerUseCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customerUseCase = new CustomerUseCaseImpl(customerRepository);
    }

    @Test
    void shouldCreateCustomer() {
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByDocumentNumber(any())).thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Customer result = customerUseCase.createCustomer(
            "Juan", "Perez", "juan@test.com", "CC", "123456789", new BigDecimal("5000000")
        );

        assertEquals("Juan", result.getFirstName());
        verify(customerRepository).save(any());
    }

    @Test
    void shouldThrowWhenEmailExists() {
        when(customerRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThrows(ApplicationException.class, () ->
            customerUseCase.createCustomer("Juan", "Perez", "juan@test.com", "CC", "123456789", new BigDecimal("5000000")));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDocumentExists() {
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByDocumentNumber("123456789")).thenReturn(true);

        assertThrows(ApplicationException.class, () ->
            customerUseCase.createCustomer("Juan", "Perez", "juan@test.com", "CC", "123456789", new BigDecimal("5000000")));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder().id(id).firstName("Juan").lastName("Perez").build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerUseCase.findById(id);
        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getFirstName());
    }

    @Test
    void shouldFindAll() {
        Customer customer = Customer.builder().firstName("Juan").lastName("Perez").build();
        PageResult<Customer> page = new PageResult<>(List.of(customer), 0, 10, 1);
        when(customerRepository.findAll(0, 10, null, null)).thenReturn(page);

        PageResult<Customer> result = customerUseCase.findAll(0, 10, null, null);
        assertEquals(1, result.getContent().size());
    }
}
