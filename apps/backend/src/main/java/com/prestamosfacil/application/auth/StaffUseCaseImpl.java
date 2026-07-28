package com.prestamosfacil.application.auth;

import com.prestamosfacil.domain.auth.port.in.StaffUseCase;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import com.prestamosfacil.domain.user.utils.PasswordPolicy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class StaffUseCaseImpl implements StaffUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderDomainPort passwordEncoder;

    public StaffUseCaseImpl(UserRepository userRepository, PasswordEncoderDomainPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User registerStaff(String name, String email, String password, String role) {
        PasswordPolicy.validate(password);
        String emailNorm = email.toLowerCase().trim();

        if (userRepository.findByEmail(emailNorm).isPresent()) {
            throw new ApplicationException(Messages.AUTH_EMAIL_ALREADY_STAFF);
        }

        String normalizedRole = role == null ? "ANALYST" : role.trim().toUpperCase();
        if (!Set.of("ANALYST", "CREDIT_ANALYST", "SUPERVISOR", "AUDITOR").contains(normalizedRole)) {
            throw new ApplicationException(Messages.ACCESS_DENIED);
        }
        User user = User.builder()
                .firstName(name)
                .email(new EmailAddress(emailNorm))
                .passwordHash(passwordEncoder.encode(password))
                .role(normalizedRole)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateStaffProfile(UUID id, String firstName, String lastName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(Messages.AUTH_USER_NOT_FOUND));

        User updated = User.builder()
                .id(user.getId())
                .firstName(firstName)
                .lastName(lastName)
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .blockedUntil(user.getBlockedUntil())
                .lastLoginAt(user.getLastLoginAt())
                .enabled(user.isEnabled())
                .build();

        return userRepository.save(updated);
    }
}
