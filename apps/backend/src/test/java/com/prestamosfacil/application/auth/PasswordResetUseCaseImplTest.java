package com.prestamosfacil.application.auth;

import com.prestamosfacil.domain.auth.port.out.HashCalculator;
import com.prestamosfacil.domain.auth.port.out.TokenGeneratorPort;
import com.prestamosfacil.domain.notification.port.out.NotificationPort;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.auth.models.PasswordResetToken;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.auth.port.out.PasswordResetTokenRepository;
import com.prestamosfacil.domain.auth.port.out.RefreshTokenRepository;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.customer.models.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetUseCaseImplTest {

    private CustomerRepository customerRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoderDomainPort passwordEncoder;
    private UserRepository userRepository;
    private HashCalculator hashCalculator;
    private TokenGeneratorPort tokenGenerator;
    private NotificationPort notificationPort;
    private PasswordResetUseCaseImpl passwordResetUseCase;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoderDomainPort.class);
        userRepository = mock(UserRepository.class);
        hashCalculator = mock(HashCalculator.class);
        tokenGenerator = mock(TokenGeneratorPort.class);
        notificationPort = mock(NotificationPort.class);
        passwordResetUseCase = new PasswordResetUseCaseImpl(
            customerRepository, userRepository, passwordResetTokenRepository,
            refreshTokenRepository, passwordEncoder, hashCalculator, tokenGenerator,
            notificationPort, "http://localhost:4000");

        customer = Customer.builder()
            .firstName("Juan")
            .lastName("Perez")
            .email(new EmailAddress("juan@test.com"))
            .documentNumber(new DocumentNumber("CC", "123456789"))
            .phoneNumber(new PhoneNumber("+57", "3001234567"))
            .baseSalary(new Money(new BigDecimal("5000000")))
            .user(com.prestamosfacil.domain.user.models.User.builder()
                .id(UUID.randomUUID())
                .email(new EmailAddress("juan@test.com"))
                .passwordHash("oldhash")
                .build())
            .build();
    }

    @Test
    void shouldRequestPasswordReset() {
        when(customerRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(customer));
        when(tokenGenerator.generateRandomToken()).thenReturn("mocked-random-hex-token-1234567890");
        when(hashCalculator.sha256(any())).thenReturn("hashedToken");
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = passwordResetUseCase.requestPasswordReset("juan@test.com");
        assertNotNull(result);
        assertEquals("mocked-random-hex-token-1234567890", result);
    }

    @Test
    void shouldReturnOkWhenEmailNotFound() {
        when(customerRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        String result = passwordResetUseCase.requestPasswordReset("unknown@test.com");
        assertEquals("OK", result);
    }

    @Test
    void shouldConfirmPasswordReset() {
        String token = "raw-token";
        String tokenHash = "hashedToken";
        String newPassword = "NewPass1@";
        String encodedPassword = "encodedNew";

        when(hashCalculator.sha256(token)).thenReturn(tokenHash);
        PasswordResetToken resetToken = new PasswordResetToken(customer.getUser(), tokenHash,
            Instant.now().plus(1, ChronoUnit.HOURS));
        when(passwordResetTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(resetToken));
        when(customerRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetUseCase.confirmPasswordReset(token, newPassword);

        verify(passwordResetTokenRepository).save(any());
        verify(customerRepository).save(customer);
        verify(refreshTokenRepository).revokeAllForUser(customer.getUser().getId());
    }

    @Test
    void shouldThrowOnInvalidToken() {
        String token = "raw-token";
        when(hashCalculator.sha256(token)).thenReturn("hash");
        when(passwordResetTokenRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class,
            () -> passwordResetUseCase.confirmPasswordReset(token, "NewPass1@"));
    }

    @Test
    void shouldThrowOnExpiredToken() {
        String token = "raw-token";
        String tokenHash = "hash";
        when(hashCalculator.sha256(token)).thenReturn(tokenHash);
        PasswordResetToken expiredToken = new PasswordResetToken(customer.getUser(), tokenHash,
            Instant.now().minus(1, ChronoUnit.MINUTES));
        when(passwordResetTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

        assertThrows(ApplicationException.class,
            () -> passwordResetUseCase.confirmPasswordReset(token, "NewPass1@"));
    }

    @Test
    void shouldChangePassword() {
        UUID customerId = customer.getId();
        String currentPassword = "OldPass1@";
        String newPassword = "NewPass2@";

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(currentPassword, "oldhash")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncoded");
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetUseCase.changePassword(customerId, currentPassword, newPassword);

        verify(refreshTokenRepository).revokeAllForUser(customer.getUser().getId());
    }

    @Test
    void shouldThrowOnWrongCurrentPassword() {
        UUID customerId = customer.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrong", "oldhash")).thenReturn(false);

        assertThrows(ApplicationException.class,
            () -> passwordResetUseCase.changePassword(customerId, "wrong", "NewPass1@"));
    }

    @Test
    void shouldThrowOnWeakNewPassword() {
        UUID customerId = customer.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("OldPass1@", "oldhash")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> passwordResetUseCase.changePassword(customerId, "OldPass1@", "weak"));
    }
}
