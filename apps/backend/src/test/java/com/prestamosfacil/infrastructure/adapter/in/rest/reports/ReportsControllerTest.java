package com.prestamosfacil.infrastructure.adapter.in.rest.reports;

import com.prestamosfacil.domain.reporting.models.ApprovedLoansTotal;
import com.prestamosfacil.domain.reporting.port.out.ApprovedLoanReportPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportsControllerTest {

    private MockMvc mockMvc;
    private ApprovedLoanReportPort approvedLoanReportPort;

    @BeforeEach
    void setUp() {
        approvedLoanReportPort = mock(ApprovedLoanReportPort.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportsController(approvedLoanReportPort)).build();
    }

    @Test
    void shouldGetTotalApproved() throws Exception {
        when(approvedLoanReportPort.getTotalApproved()).thenReturn(
            new ApprovedLoansTotal(
                new BigDecimal("10000000"), 5L, Instant.now()));

        mockMvc.perform(get("/api/v1/reports/approved-loans/total"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalApprovedAmount").value(10000000))
            .andExpect(jsonPath("$.data.approvedLoansCount").value(5));
    }
}
