package org.hibernate.url_shortener_backend.service;

import org.hibernate.url_shortener_backend.model.Url;
import org.hibernate.url_shortener_backend.repository.UrlRepository;
import org.hibernate.url_shortener_backend.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

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
                    .shortCode(customAlias) // Use custom alias as short code
                    .expiresAt(expiresAt)
                    .build();

            return urlRepository.save(url);
        }

        // Generate short code using Base62
        Url url = Url.builder()
                .originalUrl(originalUrl)
                .expiresAt(expiresAt)
                .build();

        // Save to get ID
        url = urlRepository.save(url);

        // Generate short code from ID
        String shortCode = Base62Encoder.encode(url.getId());
        url.setShortCode(shortCode);

        return urlRepository.save(url);
    }

    @Transactional
    public Optional<String> getOriginalUrl(String shortCode) {
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);

        if (urlOpt.isEmpty()) {
            return Optional.empty();
        }

        Url url = urlOpt.get();

        // Check expiration
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        // Increment click count
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return Optional.of(url.getOriginalUrl());
    }
}