package com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

class LoginRateLimitFilterTest {

    private static final String LOGIN_URI = "/api/v1/auth/login";

    private MockHttpServletRequest loginRequest(String remoteAddr, String forwardedFor) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", LOGIN_URI);
        req.setRequestURI(LOGIN_URI);
        req.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            req.addHeader("X-Forwarded-For", forwardedFor);
        }
        return req;
    }

    private void simulateFailedLogin(LoginRateLimitFilter filter, MockHttpServletRequest req) throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        // Emulate the wrapped controller responding 401 (bad credentials) for every attempt.
        org.mockito.Mockito.doAnswer(invocation -> {
            res.setStatus(401);
            return null;
        }).when(chain).doFilter(req, res);
        filter.doFilter(req, res, chain);
    }

    @Test
    void untrustedForwardedForHeaderIsIgnoredAndDoesNotBypassRateLimit() throws Exception {
        // No trusted proxies configured: the filter must rely on the socket's remote address,
        // not the attacker-controlled X-Forwarded-For header, when counting failed attempts.
        LoginRateLimitFilter filter = new LoginRateLimitFilter("");
        String attackerSocketIp = "203.0.113.9";

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest req = loginRequest(attackerSocketIp, "1.1.1." + i);
            simulateFailedLogin(filter, req);
        }

        MockHttpServletRequest blockedReq = loginRequest(attackerSocketIp, "9.9.9.9");
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(blockedReq, blockedRes, chain);

        assertEquals(429, blockedRes.getStatus(),
            "Spoofing X-Forwarded-For must not reset or evade the per-socket rate limit");
    }

    @Test
    void forwardedForIsOnlyHonoredWhenRequestComesFromTrustedProxy() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter("10.0.0.1");

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest req = loginRequest("10.0.0.1", "198.51.100.1");
            simulateFailedLogin(filter, req);
        }

        // A different real client IP behind the same trusted proxy must not be blocked.
        MockHttpServletRequest otherClientReq = loginRequest("10.0.0.1", "198.51.100.2");
        MockHttpServletResponse otherClientRes = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(otherClientReq, otherClientRes, chain);

        assertNotEquals(429, otherClientRes.getStatus());
    }
}
