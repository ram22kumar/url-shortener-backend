package org.hibernate.url_shortener_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:";
    private static final int MAX_REQUESTS = 10; // 10 requests
    private static final int WINDOW_SECONDS = 60; // per minute

    public boolean isAllowed(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + ipAddress;

        try {
            String countStr = redisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;

            if (count >= MAX_REQUESTS) {
                log.warn("Rate limit exceeded for IP: {}", ipAddress);
                return false;
            }

            // Increment counter
            if (count == 0) {
                // First request, set with expiry
                redisTemplate.opsForValue().set(key, "1", WINDOW_SECONDS, TimeUnit.SECONDS);
            } else {
                // Increment existing counter
                redisTemplate.opsForValue().increment(key);
            }

            log.info("Rate limit check - IP: {}, Count: {}/{}", ipAddress, count + 1, MAX_REQUESTS);
            return true;

        } catch (Exception e) {
            log.error("Error checking rate limit: {}", e.getMessage());
            return true; // Allow on error (fail open)
        }
    }

    public int getRemainingRequests(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + ipAddress;
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        return Math.max(0, MAX_REQUESTS - count);
    }
}