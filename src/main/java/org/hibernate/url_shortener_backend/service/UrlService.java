package org.hibernate.url_shortener_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.url_shortener_backend.model.Url;
import org.hibernate.url_shortener_backend.repository.UrlRepository;
import org.hibernate.url_shortener_backend.util.Base62Encoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final RedisService redisService;

    @Transactional
    public Url shortenUrl(String originalUrl, String customAlias, LocalDateTime expiresAt) {
        // Check if custom alias is provided and available
        if (customAlias != null && !customAlias.isEmpty()) {
            if (urlRepository.existsByCustomAlias(customAlias)) {
                throw new RuntimeException("Custom alias already exists");
            }

            Url url = Url.builder()
                    .originalUrl(originalUrl)
                    .customAlias(customAlias)
                    .shortCode(customAlias)
                    .expiresAt(expiresAt)
                    .clickCount(0L)
                    .build();

            url = urlRepository.save(url);

            // Cache the URL
            redisService.cacheUrl(url.getShortCode(), url.getOriginalUrl());

            return url;
        }

        // Generate short code using Base62
        Url url = Url.builder()
                .originalUrl(originalUrl)
                .expiresAt(expiresAt)
                .clickCount(0L)
                .build();

        // Save to get ID
        url = urlRepository.save(url);

        // Generate short code from ID
        String shortCode = Base62Encoder.encode(url.getId());
        url.setShortCode(shortCode);
        url = urlRepository.save(url);

        // Cache the URL
        redisService.cacheUrl(shortCode, originalUrl);

        log.info("Created short URL: {} -> {}", shortCode, originalUrl);

        return url;
    }

    @Transactional
    public Optional<String> getOriginalUrl(String shortCode) {
        // Check cache first (FAST PATH)
        String cachedUrl = redisService.getCachedUrl(shortCode);
        if (cachedUrl != null) {
            log.info("✅ Cache HIT for: {}", shortCode);

            // Increment counter asynchronously (don't block redirect)
            incrementClickCount(shortCode);

            return Optional.of(cachedUrl);
        }

        log.info("❌ Cache MISS for: {}", shortCode);

        // Cache miss - query database
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);

        if (urlOpt.isEmpty()) {
            return Optional.empty();
        }

        Url url = urlOpt.get();

        // Check expiration
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        // Cache for next time
        redisService.cacheUrl(shortCode, url.getOriginalUrl());

        // Increment click count
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return Optional.of(url.getOriginalUrl());
    }

    private void incrementClickCount(String shortCode) {
        // This would normally be async (@Async) but keeping simple for now
        urlRepository.findByShortCode(shortCode).ifPresent(url -> {
            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);
        });
    }
}