package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.enums.RateType;
import com.prestamosfacil.domain.loantype.port.in.LoanTypeUseCase;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.LoanTypeController;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.mapper.LoanTypeResponseMapper;
import org.mapstruct.factory.Mappers;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoanTypeControllerTest {

    private MockMvc mockMvc;
    private LoanTypeUseCase loanTypeUseCase;
    private LoanTypeResponseMapper loanTypeResponseMapper;

    @BeforeEach
    void setUp() {
        loanTypeUseCase = mock(LoanTypeUseCase.class);
        loanTypeResponseMapper = Mappers.getMapper(LoanTypeResponseMapper.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LoanTypeController(loanTypeUseCase, loanTypeResponseMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnAllActiveLoanTypes() throws Exception {
        LoanType lt = LoanType.builder()
            .id(UUID.randomUUID())
            .name("Personal")
            .description("Desc")
            .interestRate(new BigDecimal("12.00"))
            .rateType(RateType.EA)
            .minAmount(new Money(new BigDecimal("1000000")))
            .maxAmount(new Money(new BigDecimal("50000000")))
            .active(true)
            .build();

        when(loanTypeUseCase.findAllActive()).thenReturn(List.of(lt));

        mockMvc.perform(get("/api/v1/loan-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("Personal"))
            .andExpect(jsonPath("$.data[0].rateType").value("EA"));
    }

    @Test
    void shouldReturnAdminLoanTypes() throws Exception {
        LoanType lt = LoanType.builder()
            .id(UUID.randomUUID())
            .name("Personal")
            .interestRate(new BigDecimal("12.00"))
            .minAmount(new Money(new BigDecimal("1000000")))
            .maxAmount(new Money(new BigDecimal("50000000")))
            .active(true)
            .build();

        PageResult<LoanType> pageResult = new PageResult<>(List.of(lt), 0, 10, 1);
        when(loanTypeUseCase.findAllAdmin(any(), any(), eq(0), eq(10), any(), any())).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/loan-types/admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("Personal"));
    }

    @Test
    void shouldReturnLoanTypeById() throws Exception {
        UUID id = UUID.randomUUID();
        LoanType loanType = LoanType.builder()
            .id(id)
            .name("Personal")
            .interestRate(new BigDecimal("12.00"))
            .minAmount(new Money(new BigDecimal("1000000")))
            .maxAmount(new Money(new BigDecimal("50000000")))
            .build();

        when(loanTypeUseCase.findById(id)).thenReturn(Optional.of(loanType));

        mockMvc.perform(get("/api/v1/loan-types/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Personal"));
    }

    @Test
    void shouldCreateLoanType() throws Exception {
        LoanType created = LoanType.builder()
            .id(UUID.randomUUID())
            .name("Nuevo Tipo")
            .description("Descripcion")
            .interestRate(new BigDecimal("15.00"))
            .rateType(RateType.EA)
            .minAmount(new Money(new BigDecimal("1000000")))
            .maxAmount(new Money(new BigDecimal("20000000")))
            .minTermMonths(6)
            .maxTermMonths(36)
            .displayOrder(1)
            .active(true)
            .build();

        when(loanTypeUseCase.create(any())).thenReturn(created);

        String jsonPayload = """
            {
                "name": "Nuevo Tipo",
                "description": "Descripcion",
                "interestRate": 15.00,
                "rateType": "EA",
                "minAmount": 1000000,
                "maxAmount": 20000000,
                "minTermMonths": 6,
                "maxTermMonths": 36,
                "displayOrder": 1,
                "active": true
            }
            """;

        mockMvc.perform(post("/api/v1/loan-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("Nuevo Tipo"));
    }

    @Test
    void shouldToggleLoanTypeStatus() throws Exception {
        UUID id = UUID.randomUUID();
        LoanType updated = LoanType.builder()
            .id(id)
            .name("Tipo")
            .active(false)
            .interestRate(new BigDecimal("10.00"))
            .minAmount(new Money(new BigDecimal("100")))
            .maxAmount(new Money(new BigDecimal("1000")))
            .build();

        when(loanTypeUseCase.toggleStatus(eq(id), eq(false))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/loan-types/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false));
    }
}
