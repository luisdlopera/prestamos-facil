package com.prestamosfacil.infrastructure.adapter.in.rest.staff;

import com.prestamosfacil.domain.auth.port.in.StaffUseCase;
import com.prestamosfacil.domain.auth.port.in.UserAuthUseCase;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import com.prestamosfacil.infrastructure.shared.rest.ApiResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.cookie.AuthCookieFactory;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;
import com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.request.StaffLoginRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.response.StaffLoginResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response.RefreshResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.request.StaffRegisterRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.staff.dto.response.StaffUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = SwaggerDocs.TAG_STAFF, description = SwaggerDocs.TAG_STAFF_DESC)
public class StaffAuthController {

    private final UserAuthUseCase userAuthUseCase;
    private final StaffUseCase staffUseCase;
    private final AuthCookieFactory authCookieFactory;

    public StaffAuthController(UserAuthUseCase userAuthUseCase,
                                StaffUseCase staffUseCase,
                                AuthCookieFactory authCookieFactory) {
        this.userAuthUseCase = userAuthUseCase;
        this.staffUseCase = staffUseCase;
        this.authCookieFactory = authCookieFactory;
    }

    @PostMapping("/register")
    @Operation(summary = SwaggerDocs.OP_STAFF_REGISTER_SUM, description = SwaggerDocs.OP_STAFF_REGISTER_DESC)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = SwaggerDocs.RESP_STAFF_REGISTER_201),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = SwaggerDocs.RESP_STAFF_REGISTER_400)
    })
    public ResponseEntity<ApiResponse<StaffUserResponse>> register(@Valid @RequestBody StaffRegisterRequest req) {
        var user = staffUseCase.registerStaff(req.name(), req.email(), req.password(), req.role());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(new StaffUserResponse(user.getId(), user.getName(), user.getEmail().getValue(), user.isEnabled()), Messages.SUCCESS_STAFF_REGISTERED));
    }

    @PostMapping("/login")
    @Operation(summary = SwaggerDocs.OP_STAFF_LOGIN_SUM, description = SwaggerDocs.OP_STAFF_LOGIN_DESC)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SwaggerDocs.RESP_STAFF_LOGIN_200),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = SwaggerDocs.RESP_STAFF_LOGIN_401)
    })
    public ResponseEntity<ApiResponse<StaffLoginResponse>> login(@Valid @RequestBody StaffLoginRequest req, HttpServletResponse res) {
        var r = userAuthUseCase.login(req.email(), req.password());
        authCookieFactory.setRefreshCookie(res, r.refreshToken());

        var user = staffUseCase.findById(r.aggregateId())
                .orElseThrow(() -> new ApplicationException(Messages.AUTH_STAFF_NOT_FOUND));
        var response = new StaffLoginResponse(
            user.getId(), user.getName(), user.getEmail().getValue(), user.getRole(), user.isEnabled(), r.accessToken(), r.expiresIn());
        return ResponseEntity.ok(ApiResponse.success(response, Messages.SUCCESS_LOGIN));
    }

    @PostMapping("/refresh")
    @Operation(summary = SwaggerDocs.OP_STAFF_REFRESH_SUM, description = SwaggerDocs.OP_STAFF_REFRESH_DESC)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SwaggerDocs.RESP_STAFF_REFRESH_200),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = SwaggerDocs.RESP_STAFF_REFRESH_401)
    })
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(HttpServletRequest req, HttpServletResponse res) {
        String token = authCookieFactory.extractRefreshToken(req);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_SESSION_EXPIRED));
        }
        var r = userAuthUseCase.refresh(token);
        authCookieFactory.setRefreshCookie(res, r.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(
            new RefreshResponse(r.accessToken(), r.expiresIn()), Messages.SUCCESS_REFRESH));
    }

    @PostMapping("/logout")
    @Operation(summary = SwaggerDocs.OP_STAFF_LOGOUT_SUM, description = SwaggerDocs.OP_STAFF_LOGOUT_DESC)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = SwaggerDocs.RESP_STAFF_LOGOUT_204)
    public void logout(@AuthenticationPrincipal AuthPrincipal p, HttpServletResponse res) {
        if (p != null) userAuthUseCase.logoutAll(p.userId());
        authCookieFactory.clearRefreshCookie(res);
    }

    @GetMapping("/me")
    @Operation(summary = SwaggerDocs.OP_STAFF_ME_SUM, description = SwaggerDocs.OP_STAFF_ME_DESC)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SwaggerDocs.RESP_STAFF_ME_200),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = SwaggerDocs.RESP_STAFF_ME_401)
    })
    public ResponseEntity<ApiResponse<StaffUserResponse>> me(@AuthenticationPrincipal AuthPrincipal p) {
        if (p == null || p.userType() != UserType.STAFF && p.userType() != UserType.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_NOT_AUTHENTICATED));
        }
        return staffUseCase.findById(p.userId())
            .map(u -> ResponseEntity.ok(ApiResponse.success(
                new StaffUserResponse(u.getId(), u.getName(), u.getEmail().getValue(), u.isEnabled()), Messages.SUCCESS_OK)))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(Messages.AUTH_USER_NOT_FOUND)));
    }
}
