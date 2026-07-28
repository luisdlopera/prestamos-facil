package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.LoanTypeController;
import com.prestamosfacil.infrastructure.adapter.in.rest.loantype.mapper.LoanTypeResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.shared.ResponseEnvelopeAdvice;
import org.mapstruct.factory.Mappers;
import com.prestamosfacil.domain.loantype.port.in.LoanTypeUseCase;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ResponseEnvelopeAdviceTest {

    private MockMvc mockMvc;
    private LoanTypeUseCase loanTypeUseCase;

    @BeforeEach
    void setUp() {
        loanTypeUseCase = mock(LoanTypeUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LoanTypeController(loanTypeUseCase, Mappers.getMapper(LoanTypeResponseMapper.class)))
            .setControllerAdvice(new ResponseEnvelopeAdvice())
            .build();
    }

    @Test
    void shouldWrapResponseInApiResponse() throws Exception {
        List<LoanType> types = List.of(
            new LoanType("Personal", new BigDecimal("12.0"),
                new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000"))));
        when(loanTypeUseCase.findAllActive()).thenReturn(types);

        mockMvc.perform(get("/api/v1/loan-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data[0].name").value("Personal"));
    }
}
