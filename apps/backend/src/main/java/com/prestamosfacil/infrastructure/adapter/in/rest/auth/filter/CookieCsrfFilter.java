package com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter;

import com.prestamosfacil.domain.shared.enums.Messages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CookieCsrfFilter extends OncePerRequestFilter {

    private final List<String> allowedOrigins;

    private static final List<String> COOKIE_PATHS = List.of(
        "/api/v1/auth/refresh", "/api/v1/auth/logout",
        "/api/v1/staff/refresh", "/api/v1/staff/logout"
    );

    public CookieCsrfFilter(@Value("${app.cors.allowed-origins:http://localhost:4000}") String origins) {
        this.allowedOrigins = List.of(origins.split(","));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String path = req.getRequestURI();
        boolean isCookieEndpoint = COOKIE_PATHS.stream().anyMatch(path::startsWith);

        if (isCookieEndpoint && "POST".equalsIgnoreCase(req.getMethod())) {
            String origin = req.getHeader("Origin");
            String referer = req.getHeader("Referer");

            boolean validOrigin = origin != null && allowedOrigins.stream()
                .anyMatch(o -> matchesOrigin(origin, o.trim()));

            boolean validReferer = referer != null && allowedOrigins.stream()
                .anyMatch(o -> matchesOrigin(referer, o.trim()));

            if (!validOrigin && !validReferer) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("application/json");
                String json = "{\"data\":null,\"message\":\"" + Messages.ACCESS_ORIGIN_NOT_ALLOWED.getValue()
                    + "\",\"timestamp\":\"" + Instant.now() + "\",\"errors\":null}";
                res.getWriter().write(json);
                return;
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * Compares scheme+host+port only, via URI parsing, to avoid string-prefix bypasses
     * (e.g. Referer: https://attacker.com/https://trusted.com/ matching a startsWith check).
     */
    private boolean matchesOrigin(String candidate, String allowedOrigin) {
        try {
            URI candidateUri = new URI(candidate);
            URI allowedUri = new URI(allowedOrigin);
            if (candidateUri.getScheme() == null || candidateUri.getHost() == null) {
                return false;
            }
            return candidateUri.getScheme().equalsIgnoreCase(allowedUri.getScheme())
                && candidateUri.getHost().equalsIgnoreCase(allowedUri.getHost())
                && candidateUri.getPort() == allowedUri.getPort();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
