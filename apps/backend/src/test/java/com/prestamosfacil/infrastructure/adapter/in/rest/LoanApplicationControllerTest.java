package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.domain.loanapplication.port.in.LoanApplicationUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.loanapplication.models.AutomaticEvaluationOutcome;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.LoanApplicationController;
import com.prestamosfacil.infrastructure.adapter.in.rest.loanapplication.mapper.LoanApplicationResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.CustomerContextResolver;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.GlobalExceptionHandler;
import org.mapstruct.factory.Mappers;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoanApplicationControllerTest {

    private MockMvc mockMvc;
    private LoanApplicationUseCase loanApplicationUseCase;
    private LoanApplicationResponseMapper loanApplicationResponseMapper;

    private final Customer customer = Customer.builder()
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
    private final LoanType loanType = new LoanType("Personal", new BigDecimal("12.0"),
        new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000")));
    private CustomerContextResolver customerContextResolver;

    @BeforeEach
    void setUp() {
        loanApplicationUseCase = mock(LoanApplicationUseCase.class);
        loanApplicationResponseMapper = Mappers.getMapper(LoanApplicationResponseMapper.class);
        customerContextResolver = mock(CustomerContextResolver.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LoanApplicationController(loanApplicationUseCase, loanApplicationResponseMapper, customerContextResolver))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void shouldCreateApplication() throws Exception {
        LoanApplication app = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        when(loanApplicationUseCase.createApplication(any(), any(), any(), anyInt()))
            .thenReturn(app);

        String jsonPayload = """
            {
                "customerId": "%s",
                "loanTypeId": "%s",
                "requestedAmount": 10000000,
                "termInMonths": 12
            }
            """.formatted(customer.getId(), loanType.getId());

        mockMvc.perform(post("/api/v1/loan-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldFindAllApplications() throws Exception {
        LoanApplication app = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        PageResult<LoanApplication> page = new PageResult<>(List.of(app), 0, 10, 1);
        when(loanApplicationUseCase.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/loan-applications"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldFindApplicationById() throws Exception {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        when(loanApplicationUseCase.findById(id)).thenReturn(Optional.of(app));

        mockMvc.perform(get("/api/v1/loan-applications/{id}", id))
            .andExpect(status().isOk());
    }

    @Test
    void shouldApproveApplication() throws Exception {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        app.approve();
        when(loanApplicationUseCase.approve(id)).thenReturn(app);

        mockMvc.perform(post("/api/v1/loan-applications/{id}/approve", id))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectApplication() throws Exception {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        app.reject("Monto excedido");
        when(loanApplicationUseCase.reject(id, "Monto excedido")).thenReturn(app);

        mockMvc.perform(post("/api/v1/loan-applications/{id}/reject", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Monto excedido\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldEvaluateAutomatically() throws Exception {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication(customer, loanType,
            new Money(new BigDecimal("10000000")), 12);
        when(loanApplicationUseCase.evaluateAutomatically(id))
            .thenReturn(new AutomaticEvaluationOutcome(
                LoanApplicationStatus.APPROVED,
                new BigDecimal("10000000"),
                new BigDecimal("15000000"),
                new BigDecimal("0"),
                new BigDecimal("0.20"),
                "Aprobado automáticamente"
            ));
        when(loanApplicationUseCase.findById(id)).thenReturn(Optional.of(app));

        mockMvc.perform(post("/api/v1/loan-applications/{id}/automatic-evaluation", id))
            .andExpect(status().isOk());
    }
}
