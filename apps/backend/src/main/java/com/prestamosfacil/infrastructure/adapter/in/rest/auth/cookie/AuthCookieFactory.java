package com.prestamosfacil.infrastructure.adapter.in.rest.auth.cookie;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {
    private final Environment env;
    private final JwtProperties properties;

    public AuthCookieFactory(Environment env, JwtProperties properties) {
        this.env = env;
        this.properties = properties;
    }

    public void setRefreshCookie(HttpServletResponse res, String refreshToken) {
        res.addHeader("Set-Cookie", c(properties.cookie().refreshName(), refreshToken, properties.refreshTokenTtlDays() * 86400L));
    }

    public void setAuthCookies(HttpServletResponse res, String accessToken, String refreshToken) {
        res.addHeader("Set-Cookie", c(properties.cookie().accessName(), accessToken, properties.accessTokenTtlMinutes() * 60L));
        res.addHeader("Set-Cookie", c(properties.cookie().refreshName(), refreshToken, properties.refreshTokenTtlDays() * 86400L));
    }

    public void clearRefreshCookie(HttpServletResponse res) {
        res.addHeader("Set-Cookie", c(properties.cookie().refreshName(), "", 0L));
    }

    public void clearAuthCookies(HttpServletResponse res) {
        res.addHeader("Set-Cookie", c(properties.cookie().accessName(), "", 0L));
        res.addHeader("Set-Cookie", c(properties.cookie().refreshName(), "", 0L));
    }

    public String extractRefreshToken(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> properties.cookie().refreshName().equals(c.getName())).map(c -> (c.getValue() != null && !c.getValue().isBlank()) ? c.getValue() : null)
                .findFirst().orElse(null);
    }

    private String c(String name, String value, long maxAgeSecs) {
        return ResponseCookie.from(name, value).httpOnly(properties.cookie().httpOnly()).secure(isSecure())
                .sameSite(properties.cookie().sameSite()).path(properties.cookie().path()).maxAge(Duration.ofSeconds(maxAgeSecs)).build().toString();
    }

    private boolean isSecure() {
        String[] p = env.getActiveProfiles();
        if (p == null || p.length == 0) return properties.cookie().secure();
        boolean dev = Arrays.stream(p).anyMatch(x -> x.equalsIgnoreCase("dev")||x.equalsIgnoreCase("local")||x.equalsIgnoreCase("test"));
        return dev ? properties.cookie().secure() : true;
    }
}
