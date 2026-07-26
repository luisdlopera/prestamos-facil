package com.prestamosfacil.infrastructure.adapter.in.rest.auth;

import com.prestamosfacil.domain.auth.models.LoginResult;
import com.prestamosfacil.domain.auth.port.in.PasswordResetUseCase;
import com.prestamosfacil.domain.auth.port.in.StaffUseCase;
import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.in.ProfileUseCase;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.mapper.AuthResponseMapper;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response.AuthUserResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request.ChangePasswordRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request.LoginRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response.LoginResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request.PasswordResetConfirm;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request.PasswordResetRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response.RefreshResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request.RegisterRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.request.UpdateProfileRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.response.StaffLoginResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.cookie.AuthCookieFactory;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = SwaggerDocs.TAG_AUTH, description = SwaggerDocs.TAG_AUTH_DESC)
public class AuthController {

    private final UserAuthUseCase userAuthUseCase;
    private final ProfileUseCase profileUseCase;
    private final AuthCookieFactory authCookieFactory;
    private final PasswordResetUseCase passwordResetUseCase;
    private final AuthResponseMapper authResponseMapper;
    private final StaffUseCase staffUseCase;

    @Autowired
    public AuthController(UserAuthUseCase userAuthUseCase,
                          ProfileUseCase profileUseCase,
                          AuthCookieFactory authCookieFactory,
                          PasswordResetUseCase passwordResetUseCase,
                          AuthResponseMapper authResponseMapper,
                          StaffUseCase staffUseCase) {
        this.userAuthUseCase = userAuthUseCase;
        this.profileUseCase = profileUseCase;
        this.authCookieFactory = authCookieFactory;
        this.passwordResetUseCase = passwordResetUseCase;
        this.authResponseMapper = authResponseMapper;
        this.staffUseCase = staffUseCase;
    }

    // Kept for existing unit tests and lightweight controller construction.
    public AuthController(UserAuthUseCase userAuthUseCase,
                          ProfileUseCase profileUseCase,
                          AuthCookieFactory authCookieFactory,
                          PasswordResetUseCase passwordResetUseCase,
                          AuthResponseMapper authResponseMapper) {
        this(userAuthUseCase, profileUseCase, authCookieFactory, passwordResetUseCase,
                authResponseMapper, null);
    }

    @PostMapping("/register")
    @Operation(summary = SwaggerDocs.OP_REGISTER_SUM, description = SwaggerDocs.OP_REGISTER_DESC)
    public ResponseEntity<ApiResponse<AuthUserResponse>> register(@Valid @RequestBody RegisterRequest req) {
        LoginResult r = userAuthUseCase.registerCustomer(req.firstName(), req.lastName(), req.email(),
                req.documentType(), req.documentNumber(), req.baseSalary(), req.password());
        var customer = profileUseCase.findById(r.aggregateId())
                .or(() -> findCustomerByUserId(r.aggregateId()))
                .orElseThrow(() -> new ApplicationException(Messages.CUSTOMER_REGISTER_ERROR));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authResponseMapper.toUser(customer), Messages.SUCCESS_REGISTER));
    }

    @PostMapping("/login")
    @Operation(summary = SwaggerDocs.OP_LOGIN_SUM, description = SwaggerDocs.OP_LOGIN_DESC)
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        LoginResult r = userAuthUseCase.login(req.email(), req.password());
        authCookieFactory.setRefreshCookie(res, r.refreshToken());

        // /auth/login is the public login used by the single frontend /login page.
        // Staff users are stored in users, not customers, so do not try to map them
        // through the customer profile.
        User user = staffUseCase == null ? null : staffUseCase.findById(r.aggregateId()).orElse(null);
        if (user != null && !UserType.CUSTOMER.name().equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.ok(ApiResponse.success(new StaffLoginResponse(
                    user.getId(), user.getName(), user.getEmail().getValue(),
                    user.getRole(), user.isEnabled(), r.accessToken(), r.expiresIn()), Messages.SUCCESS_LOGIN));
        }

        var customer = profileUseCase.findById(r.aggregateId())
                .or(() -> findCustomerByUserId(r.aggregateId()))
                .orElseThrow(() -> new ApplicationException(Messages.CUSTOMER_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success((Object) new LoginResponse(
                authResponseMapper.toUser(customer), r.accessToken(), r.expiresIn()), Messages.SUCCESS_LOGIN));
    }

    @PostMapping("/refresh")
    @Operation(summary = SwaggerDocs.OP_REFRESH_SUM, description = SwaggerDocs.OP_REFRESH_DESC)
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(HttpServletRequest req, HttpServletResponse res) {
        String token = authCookieFactory.extractRefreshToken(req);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_SESSION_EXPIRED));
        }
        LoginResult r = userAuthUseCase.refresh(token);
        authCookieFactory.setRefreshCookie(res, r.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(
                new RefreshResponse(r.accessToken(), r.expiresIn()), Messages.SUCCESS_REFRESH));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = SwaggerDocs.OP_LOGOUT_SUM, description = SwaggerDocs.OP_LOGOUT_DESC)
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = authCookieFactory.extractRefreshToken(req);
        userAuthUseCase.logoutByToken(refreshToken);
        authCookieFactory.clearRefreshCookie(res);
    }

    @GetMapping("/me")
    @Operation(summary = SwaggerDocs.OP_ME_SUM, description = SwaggerDocs.OP_ME_DESC)
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<AuthUserResponse>> me(@AuthenticationPrincipal AuthPrincipal p) {
        if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_NOT_AUTHENTICATED));
        if (p.userType() != UserType.CUSTOMER && staffUseCase != null) {
            return staffUseCase.findById(p.userId())
                    .map(u -> ResponseEntity.ok(ApiResponse.success(authResponseMapper.toUserFromDomain(u))))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_USER_NOT_FOUND)));
        }
        var customerOpt = profileUseCase.findById(p.userId())
                .or(() -> findCustomerByUserId(p.userId()));
        if (customerOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(authResponseMapper.toUser(customerOpt.get())));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_USER_NOT_FOUND));
    }

    @PatchMapping("/profile")
    @Operation(summary = SwaggerDocs.OP_UPDATE_PROFILE_SUM, description = SwaggerDocs.OP_UPDATE_PROFILE_DESC)
    public ResponseEntity<ApiResponse<AuthUserResponse>> updateProfile(
            @AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody UpdateProfileRequest req) {
        if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_NOT_AUTHENTICATED));

        // Handle staff users
        if (p.userType() != UserType.CUSTOMER && staffUseCase != null) {
            User user = staffUseCase.updateStaffProfile(p.userId(), req.firstName(), req.lastName());
            return ResponseEntity.ok(ApiResponse.success(authResponseMapper.toStaffUser(user), Messages.SUCCESS_PROFILE_UPDATED));
        }

        // Handle customer users
        var customer = profileUseCase.findById(p.userId())
                .or(() -> findCustomerByUserId(p.userId()))
                .orElseThrow(() -> new ApplicationException(Messages.CUSTOMER_NOT_FOUND));
        var c = profileUseCase.updateProfile(customer.getId(), req.firstName(), req.lastName(), req.baseSalary(), req.email());
        return ResponseEntity.ok(ApiResponse.success(authResponseMapper.toUser(c), Messages.SUCCESS_PROFILE_UPDATED));
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = SwaggerDocs.OP_REQUEST_PASSWORD_RESET_SUM, description = SwaggerDocs.OP_REQUEST_PASSWORD_RESET_DESC)
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest req) {
        passwordResetUseCase.requestPasswordReset(req.email());
        return ResponseEntity.ok(ApiResponse.success(null, Messages.SUCCESS_PASSWORD_RESET_REQUESTED.getValue()));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = SwaggerDocs.OP_CONFIRM_PASSWORD_RESET_SUM, description = SwaggerDocs.OP_CONFIRM_PASSWORD_RESET_DESC)
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirm req) {
        passwordResetUseCase.confirmPasswordReset(req.token(), req.newPassword());
        return ResponseEntity.ok(ApiResponse.success(null, Messages.SUCCESS_PASSWORD_RESET_CONFIRMED.getValue()));
    }

    @PostMapping("/change-password")
    @Operation(summary = SwaggerDocs.OP_CHANGE_PASSWORD_SUM, description = SwaggerDocs.OP_CHANGE_PASSWORD_DESC)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody ChangePasswordRequest req) {
        if (p == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_NOT_AUTHENTICATED));
        passwordResetUseCase.changePassword(p.userId(), req.currentPassword(), req.newPassword());
        return ResponseEntity.ok(ApiResponse.success(null, Messages.SUCCESS_PASSWORD_CHANGED.getValue()));
    }

    private Optional<Customer> findCustomerByUserId(java.util.UUID userId) {
        return profileUseCase.findByUserId(userId);
    }
}
