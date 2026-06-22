package com.messalas.omniapi.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimitFilter implements Filter {

    // POST /api/auth/login: 5 attempts per minute per IP (A07 brute-force protection)
    private static final int LOGIN_CAPACITY = 5;
    // Write ops on /api/rest/**: 20 per minute per IP (A04 abuse protection)
    private static final int WRITE_CAPACITY = 20;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> writeBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ip = resolveClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        Bucket bucket = selectBucket(ip, path, method);

        if (bucket != null) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (!probe.isConsumed()) {
                long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
                response.setStatus(429);
                response.setContentType("application/json");
                response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.getWriter().write("{\"error\":\"Too many requests\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private Bucket selectBucket(String ip, String path, String method) {
        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            return loginBuckets.computeIfAbsent(ip, k -> newBucket(LOGIN_CAPACITY));
        }
        if (isWriteMethod(method) && path.startsWith("/api/rest/")) {
            return writeBuckets.computeIfAbsent(ip, k -> newBucket(WRITE_CAPACITY));
        }
        return null;
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private Bucket newBucket(int capacity) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, REFILL_PERIOD)
                        .build())
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
