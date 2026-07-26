package com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter;

import com.prestamosfacil.infrastructure.security.jwt.TokenValidationService;
import com.prestamosfacil.infrastructure.security.principal.AuthPrincipal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Nonnull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenValidationService tokenValidationService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    private final Cache<UUID, Boolean> blockedCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS).maximumSize(10000).build();

    public JwtAuthFilter(TokenValidationService tokenValidationService,
                         UserRepository userRepository,
                         CustomerRepository customerRepository) {
        this.tokenValidationService = tokenValidationService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse res,
                                    @Nonnull FilterChain chain) throws ServletException, IOException {
        String token = extractAccessToken(req);
        if (token == null) { chain.doFilter(req, res); return; }

        Map<String, Object> claims;
        try { claims = tokenValidationService.parse(token); }
        catch (Exception ex) { chain.doFilter(req, res); return; }

        if ("refresh".equals(claims.get("typ"))) { chain.doFilter(req, res); return; }

        String sub = (String) claims.get("sub");
        String type = (String) claims.get("type");
        if (sub == null || type == null) { chain.doFilter(req, res); return; }

        UUID userId;
        try { userId = UUID.fromString(sub); }
        catch (Exception ex) { chain.doFilter(req, res); return; }

        String email = (String) claims.get("email");
        List<GrantedAuthority> authorities = new ArrayList<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            chain.doFilter(req, res);
            return;
        }

        UserType userType;
        if ("staff".equals(type)) {
            userType = UserType.STAFF;
            authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
            String role = user.getRole();
            if ("ADMIN".equalsIgnoreCase(role)) {
                userType = UserType.ADMIN;
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if (role != null && !role.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
            }
        } else {
            Boolean cachedBlocked = blockedCache.getIfPresent(userId);
            if (cachedBlocked != null && cachedBlocked) { chain.doFilter(req, res); return; }
            if (cachedBlocked == null) {
                var customerOpt = customerRepository.findByUserId(userId);
                if (customerOpt.isEmpty()) {
                    blockedCache.put(userId, true);
                    chain.doFilter(req, res); return;
                }
                blockedCache.put(userId, false);
            }
            userType = UserType.CUSTOMER;
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthPrincipal(userId, email, userType, authorities), null, authorities));
        req.setAttribute("currentUserEmail", email);
        req.setAttribute("currentUserId", userId.toString());
        req.setAttribute("currentUserType", type);
        chain.doFilter(req, res);
    }

    private String extractAccessToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!token.isBlank()) return token;
        }
        return null;
    }
}
