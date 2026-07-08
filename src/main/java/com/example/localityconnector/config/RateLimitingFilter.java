package com.example.localityconnector.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token-bucket rate limiter per client IP. No external dependencies required.
 *
 * <p>Limits are configured via properties:
 * <ul>
 *   <li>{@code app.rate-limit.requests-per-minute} — bucket size (default 60)</li>
 *   <li>{@code app.rate-limit.enabled} — feature toggle (default true)</li>
 * </ul>
 *
 * <p>A background cleanup runs on every 1000th request to evict stale entries.</p>
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int maxRequestsPerMinute;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(0);

    public RateLimitingFilter(
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.requests-per-minute:60}") int maxRequestsPerMinute) {
        this.enabled = enabled;
        this.maxRequestsPerMinute = Math.max(maxRequestsPerMinute, 1);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for static assets and health checks
        String path = request.getRequestURI();
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.equals("/favicon.ico") || path.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> new Bucket(maxRequestsPerMinute));

        if (!bucket.tryConsume()) {
            long retryAfterSeconds = bucket.secondsUntilRefill();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"Rate limit exceeded. Try again in "
                            + retryAfterSeconds + " seconds.\",\"timestamp\":\""
                            + java.time.Instant.now() + "\"}");
            return;
        }

        // Periodic cleanup of stale buckets (every 1000 requests)
        if (requestCounter.incrementAndGet() % 1000 == 0) {
            cleanupStaleBuckets();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        // Prefer X-Forwarded-For for proxied deployments
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupStaleBuckets() {
        long cutoff = System.currentTimeMillis() - 120_000L; // 2 minutes
        buckets.entrySet().removeIf(entry -> entry.getValue().lastAccess < cutoff);
    }

    /**
     * Simple fixed-window token bucket (resets every minute).
     */
    private static class Bucket {
        private final int capacity;
        private final AtomicInteger tokens;
        private volatile long windowStart;
        private volatile long lastAccess;

        Bucket(int capacity) {
            this.capacity = capacity;
            this.tokens = new AtomicInteger(capacity);
            this.windowStart = System.currentTimeMillis();
            this.lastAccess = windowStart;
        }

        boolean tryConsume() {
            lastAccess = System.currentTimeMillis();
            maybeRefill();
            return tokens.getAndUpdate(t -> t > 0 ? t - 1 : 0) > 0;
        }

        long secondsUntilRefill() {
            long elapsed = System.currentTimeMillis() - windowStart;
            long remaining = 60_000L - elapsed;
            return Math.max(remaining / 1000, 1);
        }

        private void maybeRefill() {
            long now = System.currentTimeMillis();
            if (now - windowStart >= 60_000L) {
                synchronized (this) {
                    if (now - windowStart >= 60_000L) {
                        windowStart = now;
                        tokens.set(capacity);
                    }
                }
            }
        }
    }
}
