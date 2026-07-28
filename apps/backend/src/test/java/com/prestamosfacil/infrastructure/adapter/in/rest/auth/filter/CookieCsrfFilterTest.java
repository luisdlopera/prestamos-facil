package com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CookieCsrfFilterTest {

    private static final String REFRESH_URI = "/api/v1/auth/refresh";
    private static final String ALLOWED_ORIGIN = "http://localhost:4000";

    private MockHttpServletRequest refreshRequest(String origin, String referer) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", REFRESH_URI);
        req.setRequestURI(REFRESH_URI);
        if (origin != null) req.addHeader("Origin", origin);
        if (referer != null) req.addHeader("Referer", referer);
        return req;
    }

    @Test
    void ambiguousRefererEmbeddingTrustedOriginAsPathIsRejected() throws Exception {
        CookieCsrfFilter filter = new CookieCsrfFilter(ALLOWED_ORIGIN);
        // A naive startsWith(allowedOrigin) check would have matched this attacker-hosted referer.
        MockHttpServletRequest req = refreshRequest(null, "http://attacker.com/http://localhost:4000/path");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertEquals(403, res.getStatus());
        verifyNoInteractions(chain);
    }

    @Test
    void legitimateRefererFromAllowedOriginIsAccepted() throws Exception {
        CookieCsrfFilter filter = new CookieCsrfFilter(ALLOWED_ORIGIN);
        MockHttpServletRequest req = refreshRequest(null, "http://localhost:4000/dashboard");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNotEquals(403, res.getStatus());
        verify(chain).doFilter(req, res);
    }

    @Test
    void legitimateOriginHeaderIsAccepted() throws Exception {
        CookieCsrfFilter filter = new CookieCsrfFilter(ALLOWED_ORIGIN);
        MockHttpServletRequest req = refreshRequest(ALLOWED_ORIGIN, null);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNotEquals(403, res.getStatus());
        verify(chain).doFilter(req, res);
    }

    @Test
    void mismatchedPortIsRejectedEvenWithSameHost() throws Exception {
        CookieCsrfFilter filter = new CookieCsrfFilter(ALLOWED_ORIGIN);
        MockHttpServletRequest req = refreshRequest("http://localhost:9999", null);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertEquals(403, res.getStatus());
        verifyNoInteractions(chain);
    }
}
