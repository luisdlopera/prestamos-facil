package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.prestamosfacil.infrastructure.adapter.in.rest.loan.LoanController;
import com.prestamosfacil.infrastructure.adapter.in.rest.loan.mapper.LoanResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.CustomerContextResolver;
import org.mapstruct.factory.Mappers;

class LoanControllerTest {

    private MockMvc mockMvc;
    private LoanUseCase loanUseCase;
    private PaymentPlanUseCase paymentPlanUseCase;
    private LoanResponseMapper loanResponseMapper;

    @BeforeEach
    void setUp() {
        loanUseCase = mock(LoanUseCase.class);
        paymentPlanUseCase = mock(PaymentPlanUseCase.class);
        loanResponseMapper = Mappers.getMapper(LoanResponseMapper.class);
        CustomerContextResolver customerContextResolver = mock(CustomerContextResolver.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LoanController(loanUseCase, paymentPlanUseCase, loanResponseMapper, customerContextResolver))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private Loan createMockLoan() {
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .documentNumber(new com.prestamosfacil.domain.customer.models.DocumentNumber("CC", "12345678"))
            .baseSalary(new Money(BigDecimal.TEN))
            .build();
        com.prestamosfacil.domain.loantype.models.LoanType loanType =
            new com.prestamosfacil.domain.loantype.models.LoanType(
                "Personal", new BigDecimal("12.0"),
                new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
        LoanApplication lapp = new LoanApplication(customer, loanType, new Money(BigDecimal.TEN), 12);
        return Loan.builder()
            .id(UUID.randomUUID())
            .loanApplication(lapp)
            .customer(customer)
            .principalAmount(new Money(new BigDecimal("10000000")))
            .annualInterestRate(new BigDecimal("12.0"))
            .termInMonths(12)
            .monthlyPayment(new Money(new BigDecimal("888488.79")))
            .approvedAt(Instant.now())
            .build();
    }

    @Test
    void shouldFindAllLoans() throws Exception {
        Loan loan = createMockLoan();
        when(loanUseCase.findAll(anyInt(), anyInt(), any(), any())).thenReturn(
            new PageResult<>(List.of(loan), 0, 20, 1));

        mockMvc.perform(get("/api/v1/loans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());
    }

    @Test
    void shouldFindByFilters() throws Exception {
        Loan loan = createMockLoan();
        when(loanUseCase.findByFilters(any(), any(), anyInt(), anyInt(), any(), any()))
            .thenReturn(new PageResult<>(List.of(loan), 0, 20, 1));

        mockMvc.perform(get("/api/v1/loans")
            .param("customerId", UUID.randomUUID().toString())
            .param("search", "test"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldFindById() throws Exception {
        UUID id = UUID.randomUUID();
        Loan loan = createMockLoan();
        when(loanUseCase.findById(id)).thenReturn(Optional.of(loan));

        mockMvc.perform(get("/api/v1/loans/{id}", id))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenLoanNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(loanUseCase.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/loans/{id}", id))
            .andExpect(status().isNotFound());
    }
}
