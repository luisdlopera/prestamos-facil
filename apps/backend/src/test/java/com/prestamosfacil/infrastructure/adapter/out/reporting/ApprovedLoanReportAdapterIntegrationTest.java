package com.prestamosfacil.infrastructure.adapter.out.reporting;

import com.prestamosfacil.domain.reporting.models.ApprovedLoansTotal;
import com.prestamosfacil.domain.reporting.port.out.ApprovedLoanReportPort;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ejercita loanJpaRepository.getTotalApprovedAggregation() contra un Postgres real (Testcontainers):
 * el perfil "test" usa H2 en memoria y no reproduce este bug, que depende del driver JDBC real de
 * Postgres. Antes del fix, esta query (declarada con retorno Object[] para 2 columnas sin GROUP BY)
 * hacía que Spring Data JPA envolviera la fila en una capa extra de array (Object[]{ Object[]{count,
 * sum} }), por lo que ApprovedLoanReportAdapter siempre veía result.length == 1 y devolvía cero, sin
 * importar cuántos préstamos aprobados existieran.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ApprovedLoanReportAdapterIntegrationTest {

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
    private static final UUID LOAN_TYPE_EDUCATIVO = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");

    @Autowired
    private ApprovedLoanReportPort approvedLoanReportPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnZeroWhenNoApprovedLoansExist() {
        ApprovedLoansTotal result = approvedLoanReportPort.getTotalApproved();

        assertThat(result.approvedLoansCount()).isZero();
        assertThat(result.totalApprovedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReflectApprovedLoansInTotal() {
        UUID customerId = seedCustomer("5000000");
        UUID firstApplicationId = seedApplication(customerId, LOAN_TYPE_EDUCATIVO, new BigDecimal("2000000"), 12);
        seedApprovedLoan(customerId, firstApplicationId, new BigDecimal("2000000"), new BigDecimal("185000"));
        UUID secondApplicationId = seedApplication(customerId, LOAN_TYPE_EDUCATIVO, new BigDecimal("1000000"), 12);
        seedApprovedLoan(customerId, secondApplicationId, new BigDecimal("1000000"), new BigDecimal("92500"));

        ApprovedLoansTotal result = approvedLoanReportPort.getTotalApproved();

        assertThat(result.approvedLoansCount()).isEqualTo(2L);
        assertThat(result.totalApprovedAmount()).isEqualByComparingTo(new BigDecimal("3000000"));
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

    private void seedApprovedLoan(UUID customerId, UUID applicationId, BigDecimal principalAmount, BigDecimal monthlyPayment) {
        jdbcTemplate.update("""
            INSERT INTO loans (id, loan_application_id, customer_id, principal_amount,
                annual_interest_rate, term_in_months, monthly_payment, approved_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), applicationId, customerId,
            principalAmount, new BigDecimal("10.00"),
            12, monthlyPayment, Timestamp.from(Instant.now()));
    }
}
