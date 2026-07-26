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
import java.util.concurrent.TimeUnit;
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

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (("/api/v1/auth/login".equals(uri) || "/api/v1/staff/login".equals(uri)) && "POST".equalsIgnoreCase(req.getMethod())) {
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
            else ip = ip.split(",")[0].trim();

            boolean isLocal = "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip);

            int count = cache.get(ip, k -> 0);
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
                cache.put(ip, count + 1);
            } else if (res.getStatus() == 200) {
                cache.invalidate(ip);
            }
            return;
        }
        chain.doFilter(req, res);
    }
}
