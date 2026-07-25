package com.prestamosfacil.application.auth;

import com.prestamosfacil.domain.auth.port.in.PasswordResetUseCase;
import com.prestamosfacil.domain.notification.port.out.NotificationPort;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.auth.models.PasswordResetToken;
import com.prestamosfacil.domain.auth.port.out.PasswordResetTokenRepository;
import com.prestamosfacil.domain.auth.port.out.RefreshTokenRepository;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import com.prestamosfacil.domain.auth.port.out.HashCalculator;
import com.prestamosfacil.domain.auth.port.out.TokenGeneratorPort;
import com.prestamosfacil.domain.shared.enums.Messages;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetUseCaseImpl implements PasswordResetUseCase {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoderDomainPort passwordEncoder;
    private final HashCalculator hashCalculator;
    private final TokenGeneratorPort tokenGenerator;
    private final NotificationPort notificationPort;
    private final String frontendUrl;

    public PasswordResetUseCaseImpl(CustomerRepository customerRepository,
                                    UserRepository userRepository,
                                    PasswordResetTokenRepository passwordResetTokenRepository,
                                    RefreshTokenRepository refreshTokenRepository,
                                    PasswordEncoderDomainPort passwordEncoder,
                                    HashCalculator hashCalculator,
                                    TokenGeneratorPort tokenGenerator,
                                    NotificationPort notificationPort,
                                    @Value("${app.frontend-url:http://localhost:4000}") String frontendUrl) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.hashCalculator = hashCalculator;
        this.tokenGenerator = tokenGenerator;
        this.notificationPort = notificationPort;
        this.frontendUrl = frontendUrl;
    }

    @Override
    @Transactional
    public String requestPasswordReset(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        User user = customerRepository.findByEmail(normalizedEmail)
                .map(Customer::getUser)
                .orElseGet(() -> userRepository.findByEmail(normalizedEmail).orElse(null));
        if (user == null) return "OK";

        String rawToken = tokenGenerator.generateRandomToken();
        String tokenHash = hashCalculator.sha256(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/auth/forgot-password?token=" + rawToken;
        notificationPort.notifyPasswordResetRequested(user.getEmail().getValue(), resetLink);
        return rawToken;
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String tokenHash = hashCalculator.sha256(token);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApplicationException(Messages.AUTH_TOKEN_INVALID));

        if (!resetToken.isValid()) {
            throw new ApplicationException(Messages.AUTH_TOKEN_INVALID);
        }

        PasswordResetToken usedToken = resetToken.markUsed();
        passwordResetTokenRepository.save(usedToken);

        User user = resetToken.getUser();
        if (user != null) {
            Customer customer = customerRepository.findByEmail(user.getEmail().getValue()).orElse(null);
            if (customer != null) {
                customer.getUser().changePassword(newPassword, passwordEncoder);
                customer.getUser().markLogin();
                customerRepository.save(customer);
            } else {
                user.changePassword(newPassword, passwordEncoder);
                user.markLogin();
                userRepository.save(user);
            }
            refreshTokenRepository.revokeAllForUser(user.getId());
        }
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        // Try to find staff user first
        var userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new ApplicationException(Messages.AUTH_CURRENT_PASSWORD_WRONG);
            }
            user.changePassword(newPassword, passwordEncoder);
            userRepository.save(user);
            refreshTokenRepository.revokeAllForUser(user.getId());
            return;
        }

        // Try to find customer
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(Messages.AUTH_USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, customer.getUser().getPasswordHash())) {
            throw new ApplicationException(Messages.AUTH_CURRENT_PASSWORD_WRONG);
        }

        customer.getUser().changePassword(newPassword, passwordEncoder);
        customerRepository.save(customer);
        refreshTokenRepository.revokeAllForUser(customer.getUser().getId());
    }
}
