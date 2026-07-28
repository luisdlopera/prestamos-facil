package com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prestamosfacil.infrastructure.security.constants.SecurityConstants;
import com.prestamosfacil.domain.shared.enums.Messages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final Cache<String, Integer> cache = Caffeine.newBuilder()
            .expireAfterWrite(SecurityConstants.LOGIN_RATE_LIMIT_WINDOW_MINUTES, TimeUnit.MINUTES)
            .maximumSize(SecurityConstants.RATE_LIMIT_CACHE_MAX_SIZE)
            .build();

    private final List<String> trustedProxies;

    public LoginRateLimitFilter(@Value("${app.security.trusted-proxies:}") String trustedProxiesCsv) {
        this.trustedProxies = trustedProxiesCsv.isBlank()
                ? List.of()
                : List.of(trustedProxiesCsv.split(",")).stream().map(String::trim).toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        boolean protectedPublicAuthEndpoint = Set.of(
                "/api/v1/auth/login", "/api/v1/staff/login",
                "/api/v1/auth/register", "/api/v1/auth/refresh",
                "/api/v1/staff/refresh").contains(uri);
        if (protectedPublicAuthEndpoint && "POST".equalsIgnoreCase(req.getMethod())) {
            String remoteAddr = req.getRemoteAddr();
            String ip = remoteAddr;
            // Only trust the client-supplied X-Forwarded-For header when the request
            // arrives directly from a known reverse proxy; otherwise an attacker connecting
            // directly could spoof the header to bypass per-IP rate limiting.
            if (trustedProxies.contains(remoteAddr)) {
                String forwardedFor = req.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isBlank()) {
                    ip = forwardedFor.split(",")[0].trim();
                }
            }

            boolean isLocal = "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip);

            String key = uri + "|" + ip;
            int count = cache.get(key, k -> 0);
            if (!isLocal && count >= SecurityConstants.LOGIN_RATE_LIMIT_MAX) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String json = "{\"data\":null,\"message\":\"" + Messages.AUTH_RATE_LIMITED
                        + "\",\"timestamp\":\"" + Instant.now() + "\",\"errors\":null}";
                res.getWriter().write(json);
                return;
            }

            chain.doFilter(req, res);

            if (res.getStatus() >= 400) {
                cache.put(key, count + 1);
            } else if (res.getStatus() == 200) {
                cache.invalidate(key);
            }
            return;
        }
        chain.doFilter(req, res);
    }
}
