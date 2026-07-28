package com.prestamosfacil.application.auth;

import com.prestamosfacil.domain.auth.enums.TokenType;
import com.prestamosfacil.domain.auth.models.LoginResult;
import com.prestamosfacil.domain.auth.models.RefreshToken;
import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.auth.port.out.HashCalculator;
import com.prestamosfacil.domain.auth.port.out.RefreshTokenRepository;
import com.prestamosfacil.domain.auth.port.out.TokenGeneratorPort;
import com.prestamosfacil.domain.auth.port.out.TokenParserPort;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.shared.exception.UnauthorizedApplicationException;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.user.constants.UserConstants;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.domain.user.port.out.PasswordEncoderDomainPort;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import com.prestamosfacil.domain.user.utils.PasswordPolicy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class UserAuthenticationUseCaseImpl implements UserAuthUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoderDomainPort passwordEncoder;
    private final HashCalculator hashCalculator;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenParserPort tokenParser;
    private final CustomerRepository customerRepository;

    private final long refreshTtlDays;
    private final long accessTtlSeconds;

    public UserAuthenticationUseCaseImpl(UserRepository userRepository,
                                          RefreshTokenRepository refreshTokenRepository,
                                          PasswordEncoderDomainPort passwordEncoder,
                                          HashCalculator hashCalculator,
                                          TokenGeneratorPort tokenGenerator,
                                          TokenParserPort tokenParser,
                                          CustomerRepository customerRepository,
                                          @Value("${app.security.refresh-token-ttl-days:7}") long refreshTtlDays,
                                          @Value("${app.security.access-token-ttl-minutes:15}") long accessTtlMinutes) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.hashCalculator = hashCalculator;
        this.tokenGenerator = tokenGenerator;
        this.tokenParser = tokenParser;
        this.customerRepository = customerRepository;
        this.refreshTtlDays = refreshTtlDays;
        this.accessTtlSeconds = accessTtlMinutes * 60;
    }

    @Override
    @Transactional
    public LoginResult registerCustomer(String firstName, String lastName, String email,
                                         String documentType, String documentNumber,
                                         BigDecimal baseSalary, String password) {
        PasswordPolicy.validate(password);

        EmailAddress emailVo = new EmailAddress(email);
        DocumentNumber documentVo = new DocumentNumber(documentType, documentNumber);
        Money salaryVo = new Money(baseSalary);

        if (userRepository.findByEmail(emailVo.getValue()).isPresent()) {
            throw new ApplicationException(Messages.AUTH_EMAIL_EXISTS.format(emailVo.getValue()));
        }
        if (customerRepository.existsByDocumentNumber(documentVo.getNumber())
                || userRepository.findByDocumentNumber(documentVo.getNumber()).isPresent()) {
            throw new ApplicationException(Messages.AUTH_DOCUMENT_EXISTS.format(documentVo.getNumber()));
        }

        String passwordHash = passwordEncoder.encode(password);
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(emailVo)
                .passwordHash(passwordHash)
                .documentNumber(documentVo)
                .baseSalary(salaryVo)
                .role(UserConstants.ROLE_CUSTOMER)
                .build();
        user.validate();
        user = userRepository.save(user);
        customerRepository.save(Customer.builder()
                .id(user.getId())
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .email(emailVo)
                .documentNumber(documentVo)
                .baseSalary(salaryVo)
                .build());

        return buildLoginResult(user);
    }

    @Override
    @Transactional
    public LoginResult login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedApplicationException(Messages.AUTH_INVALID_CREDENTIALS));

        if (user.isBlocked()) {
            throw new UnauthorizedApplicationException(Messages.AUTH_ACCOUNT_BLOCKED);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new UnauthorizedApplicationException(Messages.AUTH_INVALID_CREDENTIALS);
        }

        user.resetFailedAttempts();
        user.markLogin();
        userRepository.save(user);

        return buildLoginResult(user);
    }

    @Override
    @Transactional
    public LoginResult refresh(String refreshTokenValue) {
        Map<String, Object> claims = tokenParser.parse(refreshTokenValue);
        if (!"refresh".equals(claims.get("typ"))) {
            throw new ApplicationException(Messages.AUTH_TOKEN_INVALID);
        }
        String sub = (String) claims.get("sub");
        if (sub == null) throw new ApplicationException(Messages.AUTH_TOKEN_INVALID);
        UUID tokenUserId;
        try { tokenUserId = UUID.fromString(sub); }
        catch (IllegalArgumentException ex) { throw new ApplicationException(Messages.AUTH_TOKEN_INVALID); }

        String tokenHash = hashCalculator.sha256(refreshTokenValue);
        var existingToken = refreshTokenRepository.findByTokenHash(tokenHash);

        if (existingToken.isEmpty() || !existingToken.get().isValid()) {
            // Deliberadamente no se revela si el token expiró, fue revocado o nunca existió.
            throw new ApplicationException(Messages.AUTH_SESSION_EXPIRED);
        }

        var revoked = existingToken.get().revoke();
        refreshTokenRepository.save(revoked);

        String email = (String) claims.get("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(Messages.AUTH_USER_NOT_FOUND));
        if (!user.getId().equals(tokenUserId)) throw new ApplicationException(Messages.AUTH_TOKEN_INVALID);

        return buildLoginResult(user);
    }

    @Override
    @Transactional
    public void logoutByToken(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            String tokenHash = hashCalculator.sha256(refreshToken);
            refreshTokenRepository.revokeByTokenHash(tokenHash);
        }
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    private LoginResult buildLoginResult(User user) {
        UUID sessionId = UUID.randomUUID();
        UUID refreshJti = UUID.randomUUID();
        String userEmail = user.getEmail().getValue();

        UUID userId = user.getId();

        String role = user.getRole();
        String accessToken = tokenGenerator.createAccessToken(
                userId, sessionId, userEmail, role);
        String refreshToken = tokenGenerator.createRefreshToken(
                userId, sessionId, refreshJti, role, userEmail);

        boolean isStaff = !UserConstants.ROLE_CUSTOMER.equals(user.getRole());
        TokenType tokenType = isStaff
                ? TokenType.STAFF
                : TokenType.CUSTOMER;

        RefreshToken tokenEntity = RefreshToken.builder()
                .tokenType(tokenType)
                .user(user)
                .tokenHash(hashCalculator.sha256(refreshToken))
                .expiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(tokenEntity);

        return new LoginResult(userId, user.getRole(), accessToken, refreshToken, accessTtlSeconds);
    }
}
