package com.prestamosfacil.infrastructure.adapter.out.storedprocedure;

import com.prestamosfacil.domain.loanapplication.models.EvaluationResult;
import com.prestamosfacil.domain.loanapplication.port.out.AutomaticLoanEvaluationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ejercita el Stored Procedure real sp_evaluate_loan_application contra un Postgres real
 * (Testcontainers, con Flyway corriendo todas las migraciones, incluida V20). El perfil "test"
 * del proyecto usa H2 en memoria con Flyway deshabilitado (no soporta PL/pgSQL), por eso aquí
 * se sobreescriben esas propiedades para apuntar al contenedor real.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class AutomaticLoanEvaluationAdapterIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "false");
    }

    // IDs sembrados por V4__seed_data.sql
    private static final UUID LOAN_TYPE_EDUCATIVO = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012"); // 10.00%
    private static final UUID LOAN_TYPE_MICROCREDITO = UUID.fromString("e5f6a7b8-c9d0-1234-efab-345678901234"); // 24.00%
    private static final UUID LOAN_TYPE_VEHICULO = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123"); // 16.00%

    @Autowired
    private AutomaticLoanEvaluationPort evaluationPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID zeroRateLoanTypeId;

    @BeforeEach
    void seedZeroRateLoanType() {
        zeroRateLoanTypeId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO loan_types (id, name, annual_interest_rate, automatic_validation_enabled,
                minimum_amount, maximum_amount, active, minimum_term_months, maximum_term_months)
            VALUES (?, ?, 0.00, TRUE, 100000, 10000000, TRUE, 1, 60)
            """, zeroRateLoanTypeId, "Tasa Cero " + zeroRateLoanTypeId);
    }

    @Test
    void shouldApproveWhenInstallmentFitsAvailableCapacity() {
        UUID customerId = seedCustomer("6000000"); // capacidad = 2,100,000
        UUID applicationId = seedApplication(customerId, LOAN_TYPE_EDUCATIVO,
            new BigDecimal("3000000"), 12);

        EvaluationResult result = evaluationPort.evaluate(
            applicationId, new BigDecimal("6000000"), new BigDecimal("3000000"), 12, new BigDecimal("10.00"));

        assertThat(result.decision()).isEqualTo("APPROVED");
        assertThat(result.maxCapacity()).isEqualByComparingTo(new BigDecimal("2100000.00"));
        assertThat(result.currentDebt()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.newInstallment()).isLessThanOrEqualTo(result.availableCapacity());
        assertThat(result.debtRatio()).isNotNull();
        assertThat(result.reason()).containsIgnoringCase("cumple todos los criterios");
    }

    @Test
    void shouldRejectWhenInstallmentExceedsAvailableCapacity() {
        UUID customerId = seedCustomer("800000"); // capacidad = 280,000
        UUID applicationId = seedApplication(customerId, LOAN_TYPE_MICROCREDITO,
            new BigDecimal("4000000"), 6);

        EvaluationResult result = evaluationPort.evaluate(
            applicationId, new BigDecimal("800000"), new BigDecimal("4000000"), 6, new BigDecimal("24.00"));

        assertThat(result.decision()).isEqualTo("REJECTED");
        assertThat(result.newInstallment()).isGreaterThan(result.availableCapacity());
        assertThat(result.reason()).containsIgnoringCase("capacidad de endeudamiento");
    }

    @Test
    void shouldRequireManualReviewWhenAmountExceedsFiveTimesSalary() {
        UUID customerId = seedCustomer("2000000"); // capacidad = 700,000; 5x salario = 10,000,000
        UUID applicationId = seedApplication(customerId, LOAN_TYPE_VEHICULO,
            new BigDecimal("11000000"), 84);

        EvaluationResult result = evaluationPort.evaluate(
            applicationId, new BigDecimal("2000000"), new BigDecimal("11000000"), 84, new BigDecimal("16.00"));

        assertThat(result.decision()).isEqualTo("MANUAL_REVIEW");
        assertThat(result.newInstallment()).isLessThanOrEqualTo(result.availableCapacity());
        assertThat(result.reason()).containsIgnoringCase("5 veces el salario base");
    }

    @Test
    void shouldUseSimpleDivisionWhenInterestRateIsZero() {
        UUID customerId = seedCustomer("5000000"); // capacidad = 1,750,000
        UUID applicationId = seedApplication(customerId, zeroRateLoanTypeId,
            new BigDecimal("1200000"), 12);

        EvaluationResult result = evaluationPort.evaluate(
            applicationId, new BigDecimal("5000000"), new BigDecimal("1200000"), 12, BigDecimal.ZERO);

        assertThat(result.decision()).isEqualTo("APPROVED");
        assertThat(result.newInstallment()).isEqualByComparingTo(new BigDecimal("100000.00")); // 1,200,000 / 12
    }

    @Test
    void shouldSumMonthlyPaymentsOfExistingLoansAsCurrentDebt() {
        UUID customerId = seedCustomer("5000000"); // capacidad = 1,750,000
        UUID firstApplicationId = seedApplication(customerId, LOAN_TYPE_EDUCATIVO,
            new BigDecimal("2000000"), 12);
        seedApprovedLoan(customerId, firstApplicationId, new BigDecimal("300000"));

        UUID secondApplicationId = seedApplication(customerId, LOAN_TYPE_EDUCATIVO,
            new BigDecimal("2000000"), 12);
        seedApprovedLoan(customerId, secondApplicationId, new BigDecimal("250000"));

        UUID newApplicationId = seedApplication(customerId, LOAN_TYPE_EDUCATIVO,
            new BigDecimal("3000000"), 12);

        EvaluationResult result = evaluationPort.evaluate(
            newApplicationId, new BigDecimal("5000000"), new BigDecimal("3000000"), 12, new BigDecimal("10.00"));

        // La deuda actual debe sumar los dos préstamos ya existentes del cliente (300,000 + 250,000),
        // sin depender de una columna `loans.status` (fue eliminada en V16).
        assertThat(result.currentDebt()).isEqualByComparingTo(new BigDecimal("550000.00"));
        assertThat(result.availableCapacity()).isEqualByComparingTo(new BigDecimal("1200000.00")); // 1,750,000 - 550,000
    }

    @Test
    void shouldRejectWhenApplicationDoesNotExist() {
        EvaluationResult result = evaluationPort.evaluate(
            UUID.randomUUID(), new BigDecimal("5000000"), new BigDecimal("1000000"), 12, new BigDecimal("10.00"));

        assertThat(result.decision()).isEqualTo("REJECTED");
        assertThat(result.reason()).containsIgnoringCase("no encontrada");
    }

    private UUID seedCustomer(String baseSalary) {
        UUID userId = UUID.randomUUID();
        String suffix = userId.toString();

        jdbcTemplate.update("""
            INSERT INTO users (id, name, email, password_hash, enabled, first_name,
                last_name, document_type, document_number, base_salary, role)
            VALUES (?, 'Cliente Test', ?, 'hash', TRUE, 'Cliente', 'Test', 'CC', ?, ?, 'CUSTOMER')
            """, userId, "cliente-" + suffix + "@test.com", "DOC" + suffix.substring(0, 18),
            new BigDecimal(baseSalary));

        return userId;
    }

    private UUID seedApplication(UUID customerId, UUID loanTypeId, BigDecimal requestedAmount, int termInMonths) {
        UUID applicationId = UUID.randomUUID();
        BigDecimal annualRate = jdbcTemplate.queryForObject(
            "SELECT annual_interest_rate FROM loan_types WHERE id = ?", BigDecimal.class, loanTypeId);

        jdbcTemplate.update("""
            INSERT INTO loan_applications (id, customer_id, loan_type_id, requested_amount,
                term_in_months, annual_interest_rate)
            VALUES (?, ?, ?, ?, ?, ?)
            """, applicationId, customerId, loanTypeId, requestedAmount, termInMonths, annualRate);

        return applicationId;
    }

    private void seedApprovedLoan(UUID customerId, UUID applicationId, BigDecimal monthlyPayment) {
        jdbcTemplate.update("""
            INSERT INTO loans (id, loan_application_id, customer_id, principal_amount,
                annual_interest_rate, term_in_months, monthly_payment, approved_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), applicationId, customerId,
            new BigDecimal("2000000"), new BigDecimal("10.00"),
            12, monthlyPayment, Instant.now());
    }
}
