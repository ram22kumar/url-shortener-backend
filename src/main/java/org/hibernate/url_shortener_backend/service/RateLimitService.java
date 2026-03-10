package org.hibernate.url_shortener_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(RedisTemplate.class)
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:";
    private static final int MAX_REQUESTS = 10; // 10 requests
    private static final int WINDOW_SECONDS = 60; // per minute

    public boolean isAllowed(String ipAddress) {
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            String countStr = redisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;

            if (count >= MAX_REQUESTS) {
                log.warn("Rate limit exceeded for IP: {}", ipAddress);
                return false;
            }

            if (count == 0) {
                redisTemplate.opsForValue().set(key, "1", WINDOW_SECONDS, TimeUnit.SECONDS);
            } else {
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
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            String countStr = redisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;
            return Math.max(0, MAX_REQUESTS - count);
        } catch (Exception e) {
            return MAX_REQUESTS;
        }
    }
}