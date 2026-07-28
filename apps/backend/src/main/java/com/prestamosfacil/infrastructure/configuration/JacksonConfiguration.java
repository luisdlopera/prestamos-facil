package com.prestamosfacil.infrastructure.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import org.springframework.context.annotation.Configuration;

/** Jackson bindings for domain value objects used in persisted outbox payloads. */
@Configuration
public class JacksonConfiguration {

    public static void configure(ObjectMapper objectMapper) {
        objectMapper.addMixIn(Money.class, MoneyMixin.class);
        objectMapper.addMixIn(PaymentInstallment.class, PaymentInstallmentMixin.class);
    }

    public abstract static class MoneyMixin {
        @JsonCreator
        public MoneyMixin(@JsonProperty("amount") BigDecimal amount,
                          @JsonProperty("currency") Currency currency) {}
    }

    public abstract static class PaymentInstallmentMixin {
        @JsonCreator
        public PaymentInstallmentMixin(@JsonProperty("id") UUID id,
                                       @JsonProperty("loanId") UUID loanId,
                                       @JsonProperty("installmentNumber") int installmentNumber,
                                       @JsonProperty("dueDate") LocalDate dueDate,
                                       @JsonProperty("openingBalance") Money openingBalance,
                                       @JsonProperty("paymentAmount") Money paymentAmount,
                                       @JsonProperty("principalAmount") Money principalAmount,
                                       @JsonProperty("interestAmount") Money interestAmount,
                                       @JsonProperty("closingBalance") Money closingBalance) {}
    }
}
