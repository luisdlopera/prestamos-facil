package com.prestamosfacil.application.auth;

import com.prestamosfacil.domain.auth.enums.TokenType;
import com.prestamosfacil.domain.auth.models.LoginResult;
import com.prestamosfacil.domain.auth.models.RefreshToken;
import com.prestamosfacil.domain.auth.port.out.HashCalculator;
import com.prestamosfacil.domain.auth.port.out.RefreshTokenRepository;
import com.prestamosfacil.domain.auth.port.out.TokenGeneratorPort;
import com.prestamosfacil.domain.auth.port.out.TokenParserPort;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserAuthenticationUseCaseImplTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoderDomainPort passwordEncoder;
    private HashCalculator hashCalculator;
    private TokenGeneratorPort tokenGenerator;
    private TokenParserPort tokenParser;
    private CustomerRepository customerRepository;
    private UserAuthenticationUseCaseImpl authUseCase;

    private User user;
    private Customer customer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoderDomainPort.class);
        hashCalculator = mock(HashCalculator.class);
        tokenGenerator = mock(TokenGeneratorPort.class);
        tokenParser = mock(TokenParserPort.class);
        customerRepository = mock(CustomerRepository.class);

        authUseCase = new UserAuthenticationUseCaseImpl(
            userRepository, refreshTokenRepository, passwordEncoder,
            hashCalculator, tokenGenerator, tokenParser, customerRepository,
            7L, 15L);

        user = User.builder()
            .id(UUID.randomUUID())
            .email(new EmailAddress("juan@test.com"))
            .passwordHash("encodedPass")
            .role("CUSTOMER")
            .build();

        customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(user)
            .build();
    }

    @Test
    void shouldRegisterCustomerSuccessfully() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("ValidPass1@")).thenReturn("encodedPass");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenGenerator.createAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(tokenGenerator.createRefreshToken(any(), any(), any(), any(), any())).thenReturn("refresh-token");
        when(hashCalculator.sha256(any())).thenReturn("hashed-refresh");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResult result = authUseCase.registerCustomer(
            "Juan", "Perez", "juan@test.com", "CC", "123456789",
            new BigDecimal("5000000"), "ValidPass1@");

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () ->
            authUseCase.registerCustomer("Juan", "Perez", "juan@test.com",
                "CC", "123456789", new BigDecimal("5000000"), "ValidPass1@"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDocumentAlreadyExists() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByDocumentNumber("123456789")).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () ->
            authUseCase.registerCustomer("Juan", "Perez", "juan@test.com",
                "CC", "123456789", new BigDecimal("5000000"), "ValidPass1@"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPasswordIsWeak() {
        assertThrows(IllegalArgumentException.class, () ->
            authUseCase.registerCustomer("Juan", "Perez", "juan@test.com",
                "CC", "123456789", new BigDecimal("5000000"), "weak"));
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ValidPass1@", "encodedPass")).thenReturn(true);
        when(customerRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(customer));
        when(tokenGenerator.createAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(tokenGenerator.createRefreshToken(any(), any(), any(), any(), any())).thenReturn("refresh-token");
        when(hashCalculator.sha256(any())).thenReturn("hashed-refresh");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResult result = authUseCase.login("juan@test.com", "ValidPass1@");

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(passwordEncoder).matches("ValidPass1@", "encodedPass");
    }

    @Test
    void shouldThrowOnInvalidCredentials() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(ApplicationException.class, () ->
            authUseCase.login("juan@test.com", "wrong"));
    }

    @Test
    void shouldThrowWhenAccountIsBlocked() {
        User blockedUser = User.builder()
            .email(new EmailAddress("blocked@test.com"))
            .passwordHash("hash")
            .blockedUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();

        when(userRepository.findByEmail("blocked@test.com")).thenReturn(Optional.of(blockedUser));

        assertThrows(ApplicationException.class, () ->
            authUseCase.login("blocked@test.com", "AnyPass1@"));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () ->
            authUseCase.login("unknown@test.com", "AnyPass1@"));
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String refreshValue = "valid-refresh-token";
        Map<String, Object> claims = Map.of(
            "typ", "refresh",
            "sub", user.getId().toString(),
            "email", "juan@test.com"
        );
        when(tokenParser.parse(refreshValue)).thenReturn(claims);
        when(hashCalculator.sha256(refreshValue)).thenReturn("hashed-token");

        RefreshToken refreshToken = RefreshToken.builder()
            .tokenType(TokenType.CUSTOMER)
            .user(user)
            .tokenHash("hashed-token")
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();
        when(refreshTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(customer));
        when(tokenGenerator.createAccessToken(any(), any(), any(), any())).thenReturn("new-access-token");
        when(tokenGenerator.createRefreshToken(any(), any(), any(), any(), any())).thenReturn("new-refresh-token");

        LoginResult result = authUseCase.refresh(refreshValue);

        assertNotNull(result);
        assertEquals("new-access-token", result.accessToken());
        verify(tokenParser).parse(refreshValue);
    }

    @Test
    void shouldThrowOnRefreshWithWrongTokenType() {
        String token = "wrong-typ-token";
        when(tokenParser.parse(token)).thenReturn(Map.of("typ", "access", "sub", "123"));

        assertThrows(ApplicationException.class, () ->
            authUseCase.refresh(token));
    }

    @Test
    void shouldThrowOnRefreshWithExpiredSession() {
        String token = "expired-token";
        when(tokenParser.parse(token)).thenReturn(Map.of("typ", "refresh", "sub", "123"));
        when(hashCalculator.sha256(token)).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () ->
            authUseCase.refresh(token));
    }

    @Test
    void shouldLogoutByToken() {
        String token = "some-token";
        when(hashCalculator.sha256(token)).thenReturn("hashed-token");

        authUseCase.logoutByToken(token);

        verify(refreshTokenRepository).revokeByTokenHash("hashed-token");
    }

    @Test
    void shouldNotRevokeOnNullToken() {
        authUseCase.logoutByToken(null);
        authUseCase.logoutByToken("");

        verify(refreshTokenRepository, never()).revokeByTokenHash(any());
    }

    @Test
    void shouldLogoutAll() {
        UUID userId = UUID.randomUUID();

        authUseCase.logoutAll(userId);

        verify(refreshTokenRepository).revokeAllForUser(userId);
    }

    @Test
    void shouldThrowOnRefreshWithNullSub() {
        String token = "no-sub-token";
        when(tokenParser.parse(token)).thenReturn(Map.of("typ", "refresh"));

        assertThrows(ApplicationException.class, () ->
            authUseCase.refresh(token));
    }

    @Test
    void shouldThrowWhenCustomerNotFoundInResolveSubject() {
        String refreshValue = "valid-refresh-token";
        Map<String, Object> claims = Map.of(
            "typ", "refresh",
            "sub", user.getId().toString(),
            "email", "juan@test.com"
        );
        when(tokenParser.parse(refreshValue)).thenReturn(claims);
        when(hashCalculator.sha256(refreshValue)).thenReturn("hashed-token");

        RefreshToken refreshToken = RefreshToken.builder()
            .tokenType(TokenType.CUSTOMER)
            .user(user)
            .tokenHash("hashed-token")
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();
        when(refreshTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () ->
            authUseCase.refresh(refreshValue));
    }

    @Test
    void shouldLoginAndIncrementFailedAttemptsOnWrongPassword() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(ApplicationException.class, () ->
            authUseCase.login("juan@test.com", "wrong"));

        verify(userRepository).save(user);
        assertEquals(1, user.getFailedLoginAttempts());
    }
}
