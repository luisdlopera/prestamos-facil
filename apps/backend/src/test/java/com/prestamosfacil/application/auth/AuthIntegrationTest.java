package com.prestamosfacil.application.auth;

import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.auth.models.LoginResult;
import com.prestamosfacil.domain.auth.port.in.PasswordResetUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.in.ProfileUseCase;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private UserAuthUseCase authenticationUseCase;

    @Autowired
    private ProfileUseCase profileUseCase;

    @Autowired
    private PasswordResetUseCase passwordResetUseCase;

    @Autowired
    private CustomerRepository customerRepository;


    @Test
    void registerAndLoginAndRefresh() {
        LoginResult registerResult = authenticationUseCase.registerCustomer("Juan", "Perez",
                "juan@test.com", "CC", "1234567890",
                new BigDecimal("2500000"), "TestPass1@");
        assertNotNull(registerResult);
        assertNotNull(registerResult.accessToken());
        assertNotNull(registerResult.refreshToken());

        LoginResult loginResult = authenticationUseCase.login("juan@test.com", "TestPass1@");
        assertNotNull(loginResult);
        assertNotNull(loginResult.accessToken());

        LoginResult refreshResult = authenticationUseCase.refresh(registerResult.refreshToken());
        assertNotNull(refreshResult);
        assertNotNull(refreshResult.accessToken());
    }

    @Test
    void registerWithDuplicateEmailFails() {
        authenticationUseCase.registerCustomer("Ana", "Lopez", "ana@test.com",
                "CC", "9876543210", new BigDecimal("3000000"), "TestPass2@");

        assertThrows(RuntimeException.class, () ->
                authenticationUseCase.registerCustomer("Ana2", "Lopez2", "ana@test.com",
                        "CC", "1111111111", new BigDecimal("3000000"), "TestPass2@"));
    }

    @Test
    void loginWithInvalidCredentialsFails() {
        assertThrows(RuntimeException.class, () ->
                authenticationUseCase.login("nonexistent@test.com", "WrongPass1@"));
    }

    @Test
    void findByIdAfterRegister() {
        LoginResult result = authenticationUseCase.registerCustomer("Carlos", "Gomez",
                "carlos@test.com", "CC", "5555555555",
                new BigDecimal("4000000"), "TestPass3@");
        UUID id = result.aggregateId();

        var found = customerRepository.findById(id).or(() -> customerRepository.findByUserId(id));
        assertTrue(found.isPresent());
        assertEquals("carlos@test.com", found.get().getEmail().getValue());
    }

    @Test
    void registerWithWeakPasswordFails() {
        assertThrows(RuntimeException.class, () ->
                authenticationUseCase.registerCustomer("Test", "User", "weak@test.com",
                        "CC", "9999999999", new BigDecimal("1000000"), "weak"));
    }

    @Test
    void registerWithSalaryOutOfRangeFails() {
        assertThrows(RuntimeException.class, () ->
                authenticationUseCase.registerCustomer("Test", "User", "salary@test.com",
                        "CC", "8888888888", new BigDecimal("20000000"), "TestPass4@"));
    }

    @Test
    void passwordResetFlow() {
        authenticationUseCase.registerCustomer("Reset", "User", "reset@test.com",
                "CC", "7777777777", new BigDecimal("2000000"), "TestPass5@");

        String token = passwordResetUseCase.requestPasswordReset("reset@test.com");
        assertNotNull(token);

        passwordResetUseCase.confirmPasswordReset(token, "NewPass123@");

        LoginResult loginResult = authenticationUseCase.login("reset@test.com", "NewPass123@");
        assertNotNull(loginResult);
    }

    @Test
    void updateProfile() {
        LoginResult result = authenticationUseCase.registerCustomer("Profile", "Test",
                "profile@test.com", "CC", "4444444444",
                new BigDecimal("5000000"), "Profile1@");
        UUID id = result.aggregateId();

        Customer updated = profileUseCase.updateProfile(id, "UpdatedName", null, null, null);
        assertEquals("UpdatedName", updated.getFirstName());

        updated = profileUseCase.updateProfile(id, null, "UpdatedLast", new BigDecimal("6000000"), null);
        assertEquals("UpdatedLast", updated.getLastName());
        assertEquals(0, new BigDecimal("6000000").compareTo(updated.getBaseSalary().getAmount()));

        updated = profileUseCase.updateProfile(id, null, null, null, "updatedemail@test.com");
        assertEquals("updatedemail@test.com", updated.getEmail().getValue());
    }

}
