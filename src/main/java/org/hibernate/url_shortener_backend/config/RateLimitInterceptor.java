package org.hibernate.url_shortener_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.url_shortener_backend.service.RateLimitService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only rate limit POST requests (URL creation)
        if ("POST".equals(request.getMethod()) && request.getRequestURI().contains("/api/shorten")) {
            String ipAddress = request.getRemoteAddr();

            if (!rateLimitService.isAllowed(ipAddress)) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Rate limit exceeded. Please try again later.\"}"
                );
                return false;
            }

            // Add rate limit headers
            int remaining = rateLimitService.getRemainingRequests(ipAddress);
            response.setHeader("X-RateLimit-Limit", "10");
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        }

        return true;
    }
}