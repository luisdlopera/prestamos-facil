package com.prestamosfacil.infrastructure.adapter.in.rest.staff;

import com.prestamosfacil.domain.auth.models.LoginResult;
import com.prestamosfacil.domain.auth.port.in.StaffUseCase;
import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.cookie.AuthCookieFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StaffAuthControllerTest {

    private MockMvc mockMvc;
    private UserAuthUseCase userAuthUseCase;
    private StaffUseCase staffUseCase;
    private AuthCookieFactory authCookieFactory;

    @BeforeEach
    void setUp() {
        userAuthUseCase = mock(UserAuthUseCase.class);
        staffUseCase = mock(StaffUseCase.class);
        authCookieFactory = mock(AuthCookieFactory.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
            new StaffAuthController(userAuthUseCase, staffUseCase, authCookieFactory))
            .build();
    }

    private User buildStaff() {
        return User.builder()
            .firstName("Admin")
            .email(new EmailAddress("admin@test.com"))
            .passwordHash("encoded")
            .role("ADMIN")
            .build();
    }

    @Test
    void shouldRegister() throws Exception {
        User staff = buildStaff();
        when(staffUseCase.registerStaff(any(), any(), any(), any())).thenReturn(staff);

        String json = """
            {
                "name": "Admin",
                "email": "admin@test.com",
                "password": "Strong1@Pass",
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(post("/api/v1/staff/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldLogin() throws Exception {
        UUID staffId = UUID.randomUUID();
        User staff = buildStaff();
        when(userAuthUseCase.login(any(), any()))
            .thenReturn(new LoginResult(staffId, "ADMIN", "access", "refresh", 900L));
        when(staffUseCase.findById(staffId)).thenReturn(Optional.of(staff));

        String json = """
            {
                "email": "admin@test.com",
                "password": "Strong1@Pass"
            }
            """;

        mockMvc.perform(post("/api/v1/staff/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());
    }
}
