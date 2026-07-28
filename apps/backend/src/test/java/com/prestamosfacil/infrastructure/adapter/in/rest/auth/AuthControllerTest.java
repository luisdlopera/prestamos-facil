package com.prestamosfacil.infrastructure.adapter.in.rest.auth;

import com.prestamosfacil.domain.auth.models.LoginResult;
import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.auth.port.in.PasswordResetUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.customer.port.in.ProfileUseCase;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.mapper.AuthResponseMapper;
import org.mapstruct.factory.Mappers;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.cookie.AuthCookieFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private UserAuthUseCase userAuthUseCase;
    private ProfileUseCase profileUseCase;
    private AuthCookieFactory authCookieFactory;
    private PasswordResetUseCase passwordResetUseCase;
    private AuthResponseMapper authResponseMapper;

    @BeforeEach
    void setUp() {
        userAuthUseCase = mock(UserAuthUseCase.class);
        profileUseCase = mock(ProfileUseCase.class);
        authCookieFactory = mock(AuthCookieFactory.class);
        passwordResetUseCase = mock(PasswordResetUseCase.class);
        authResponseMapper = Mappers.getMapper(AuthResponseMapper.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
            new AuthController(userAuthUseCase, profileUseCase, authCookieFactory,
                passwordResetUseCase, authResponseMapper))
            .build();
    }

    @Test
    void shouldRegister() throws Exception {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("hash")
                .build())
            .build();
        when(userAuthUseCase.registerCustomer(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new LoginResult(customerId, "CUSTOMER", "access", "refresh", 900L));
        when(profileUseCase.findById(customerId)).thenReturn(java.util.Optional.of(customer));

        String json = """
            {
                "firstName": "Juan",
                "lastName": "Perez",
                "email": "juan@test.com",
                "documentType": "CC",
                "documentNumber": "123456789",
                "phoneCountryCode": "+57",
                "phoneNumber": "3001234567",
                "baseSalary": 5000000,
                "password": "Strong1@Pass"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldLogin() throws Exception {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("hash")
                .build())
            .build();
        when(userAuthUseCase.login(any(), any()))
            .thenReturn(new LoginResult(customerId, "CUSTOMER", "access", "refresh", 900L));
        when(profileUseCase.findById(customerId)).thenReturn(java.util.Optional.of(customer));

        String json = """
            {
                "email": "juan@test.com",
                "password": "Strong1@Pass"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRequestPasswordReset() throws Exception {
        String json = """
            {
                "email": "juan@test.com"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/password-reset/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());
    }

    @Test
    void shouldConfirmPasswordReset() throws Exception {
        String json = """
            {
                "token": "reset-token",
                "newPassword": "NewPass1@"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());
    }
}
