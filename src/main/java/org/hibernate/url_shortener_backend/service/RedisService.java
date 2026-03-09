package org.hibernate.url_shortener_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String URL_PREFIX = "url:";
    private static final long CACHE_TTL = 3600; // 1 hour

    public void cacheUrl(String shortCode, String originalUrl) {
        try {
            String key = URL_PREFIX + shortCode;
            redisTemplate.opsForValue().set(key, originalUrl, CACHE_TTL, TimeUnit.SECONDS);
            log.info("Cached URL: {} -> {}", shortCode, originalUrl);
        } catch (Exception e) {
            log.error("Error caching URL: {}", e.getMessage());
        }
    }

    public String getCachedUrl(String shortCode) {
        try {
            String key = URL_PREFIX + shortCode;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting cached URL: {}", e.getMessage());
            return null;
        }
    }

    public void invalidateCache(String shortCode) {
        try {
            String key = URL_PREFIX + shortCode;
            redisTemplate.delete(key);
            log.info("Invalidated cache for: {}", shortCode);
        } catch (Exception e) {
            log.error("Error invalidating cache: {}", e.getMessage());
        }
    }
}