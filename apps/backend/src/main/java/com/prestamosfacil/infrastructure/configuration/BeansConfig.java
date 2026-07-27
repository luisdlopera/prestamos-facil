package com.prestamosfacil.infrastructure.configuration;

import com.prestamosfacil.domain.loan.models.LoanCalculator;
import com.prestamosfacil.domain.loan.utils.PaymentPlanCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public PaymentPlanCalculator paymentPlanCalculator() {
        return new PaymentPlanCalculator();
    }

    @Bean
    public LoanCalculator loanCalculator(PaymentPlanCalculator paymentPlanCalculator) {
        return new LoanCalculator(paymentPlanCalculator);
    }
}
