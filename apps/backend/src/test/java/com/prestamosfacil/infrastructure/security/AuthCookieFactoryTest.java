package com.prestamosfacil.infrastructure.security;

import com.prestamosfacil.infrastructure.configuration.properties.JwtProperties;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.cookie.AuthCookieFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthCookieFactoryTest {

    private Environment env;
    private JwtProperties props;
    private AuthCookieFactory factory;

    @BeforeEach
    void setUp() {
        env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"test"});
        props = new JwtProperties(
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QAAA==", 15, 7, 30,
            new JwtProperties.Cookie(true, "Lax", "access_token", "refresh_token", "/", true)
        );
        factory = new AuthCookieFactory(env, props);
    }

    @Test
    void shouldExtractRefreshToken() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refresh_token", "refresh-token-value");
        when(req.getCookies()).thenReturn(new Cookie[]{cookie});

        String token = factory.extractRefreshToken(req);
        assertEquals("refresh-token-value", token);
    }

    @Test
    void shouldReturnNullWhenCookiesNull() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getCookies()).thenReturn(null);

        assertNull(factory.extractRefreshToken(req));
    }

    @Test
    void shouldReturnNullWhenCookieNotFound() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("other_cookie", "value");
        when(req.getCookies()).thenReturn(new Cookie[]{cookie});

        assertNull(factory.extractRefreshToken(req));
    }

    @Test
    void shouldSetAuthCookies() {
        HttpServletResponse res = mock(HttpServletResponse.class);
        factory.setAuthCookies(res, "access-token", "refresh-token");
        verify(res, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void shouldClearAuthCookies() {
        HttpServletResponse res = mock(HttpServletResponse.class);
        factory.clearAuthCookies(res);
        verify(res, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }
}
