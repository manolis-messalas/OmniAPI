package com.messalas.omniapi.unit.security;

import com.messalas.omniapi.security.RateLimitFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Test
    void loginIsRateLimitedAfterFiveRequests() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("1.2.3.4");

        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request, blocked, chain);

        assertEquals(429, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));
        verify(chain, times(5)).doFilter(any(), any());
    }

    @Test
    void writeEndpointRateLimitedAfterTwentyRequests() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rest/books");
        request.setRemoteAddr("1.2.3.5");

        for (int i = 0; i < 20; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request, blocked, chain);

        assertEquals(429, blocked.getStatus());
        verify(chain, times(20)).doFilter(any(), any());
    }

    @Test
    void readEndpointNotRateLimited() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rest/authors");
        request.setRemoteAddr("1.2.3.6");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void differentIpsHaveIndependentBuckets() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/auth/login");
        req1.setRemoteAddr("1.2.3.4");
        for (int i = 0; i < 5; i++) {
            filter.doFilter(req1, new MockHttpServletResponse(), chain);
        }

        // A different IP should still have its full budget
        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/auth/login");
        req2.setRemoteAddr("5.6.7.8");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, chain);

        assertEquals(200, res2.getStatus());
    }

    @Test
    void xForwardedForIsUsedForIpResolution() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");

        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request, blocked, chain);

        assertEquals(429, blocked.getStatus());
        verify(chain, times(5)).doFilter(any(), any());
    }

    @Test
    void putAndDeleteAreAlsoRateLimited() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest putRequest = new MockHttpServletRequest("PUT", "/api/rest/authors/1");
        putRequest.setRemoteAddr("9.9.9.9");

        for (int i = 0; i < 20; i++) {
            filter.doFilter(putRequest, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(putRequest, blocked, chain);

        assertEquals(429, blocked.getStatus());
    }
}
