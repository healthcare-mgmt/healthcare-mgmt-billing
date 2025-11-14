package com.hms.billing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;

    // Allow 30 requests per minute (per IP)
    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public RateLimitingFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();
        String ip = req.getRemoteAddr();

        // POST /api/v1/invoices
        // GET  /api/v1/invoices/{id}/pdf
        boolean protectedEndpoint =
                ("/api/v1/invoices".equals(path) && method.equals("POST")) ||
                        (path.matches("^/api/v1/invoices/\\d+/pdf$") && method.equals("GET"));

        if (!protectedEndpoint) {
            chain.doFilter(req, res);
            return;
        }

        String key = "rl:" + ip + ":" + method + ":" + path;
        Long count = redis.opsForValue().increment(key);

        if (count == 1) {
            redis.expire(key, WINDOW);
        }

        if (count > LIMIT) {
            res.setStatus(429);
            res.getWriter().write("Too Many Requests");
            return;
        }

        chain.doFilter(req, res);
    }
}
