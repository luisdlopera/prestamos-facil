package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.domain.customer.port.in.CustomerUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.prestamosfacil.infrastructure.adapter.in.rest.customer.CustomerController;
import com.prestamosfacil.infrastructure.adapter.in.rest.customer.mapper.CustomerResponseMapper;
import org.mapstruct.factory.Mappers;

class CustomerControllerTest {

    private MockMvc mockMvc;
    private CustomerUseCase customerUseCase;
    private CustomerResponseMapper customerResponseMapper;

    @BeforeEach
    void setUp() {
        customerUseCase = mock(CustomerUseCase.class);
        customerResponseMapper = Mappers.getMapper(CustomerResponseMapper.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerUseCase, customerResponseMapper))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .build();
        when(customerUseCase.createCustomer(any(), any(), any(), any(), any(), any()))
            .thenReturn(customer);

        String json = """
            {
                "firstName": "Juan",
                "lastName": "Perez",
                "email": "juan@test.com",
                "documentType": "CC",
                "documentNumber": "123456789",
                "phoneCountryCode": "+57",
                "phoneNumber": "3001234567",
                "baseSalary": 5000000
            }
            """;

        mockMvc.perform(post("/api/v1/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.firstName").value("Juan"));
    }

    @Test
    void shouldFindById() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .build();
        when(customerUseCase.findById(id)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/v1/customers/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.firstName").value("Juan"));
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerUseCase.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAll() throws Exception {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .build();
        when(customerUseCase.findAll(anyInt(), anyInt(), any(), any())).thenReturn(
            new PageResult<>(List.of(customer), 0, 20, 1));

        mockMvc.perform(get("/api/v1/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].firstName").value("Juan"));
    }

    @Test
    void shouldFindBySearch() throws Exception {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .build();
        when(customerUseCase.findBySearch(any(), anyInt(), anyInt(), any(), any())).thenReturn(
            new PageResult<>(List.of(customer), 0, 20, 1));

        mockMvc.perform(get("/api/v1/customers").param("search", "Juan"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].firstName").value("Juan"));
    }
}
