package org.hibernate.url_shortener_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.url_shortener_backend.dto.ShortenRequest;
import org.hibernate.url_shortener_backend.model.ClickEvent;
import org.hibernate.url_shortener_backend.model.Url;
import org.hibernate.url_shortener_backend.repository.ClickRepository;
import org.hibernate.url_shortener_backend.repository.UrlRepository;
import org.hibernate.url_shortener_backend.service.AnalyticsService;
import org.hibernate.url_shortener_backend.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody ShortenRequest request) {
        try {
            Url url = urlService.shortenUrl(
                    request.getUrl(),
                    request.getCustomAlias(),
                    request.getExpiresAt()
            );

            String baseUrl = System.getenv("BASE_URL") != null ?
                    System.getenv("BASE_URL") : "http://localhost:8080";
            String shortUrl = baseUrl + "/" + url.getShortCode();

            return ResponseEntity.ok(Map.of(
                    "shortUrl", shortUrl,
                    "shortCode", url.getShortCode(),
                    "originalUrl", url.getOriginalUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{shortCode}")
    public void redirect(
            @PathVariable String shortCode,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String originalUrl = urlService.getOriginalUrl(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found or expired"));

        // Track click analytics (async - doesn't slow down redirect)
        analyticsService.trackClick(
                shortCode,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referer")
        );

        response.sendRedirect(originalUrl);
    }

    @GetMapping("/api/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        long totalClicks = clickRepository.countByShortCode(shortCode);
        List<ClickEvent> recentClicks = clickRepository
                .findByShortCodeOrderByClickedAtDesc(shortCode)
                .stream()
                .limit(10)
                .toList();

        return ResponseEntity.ok(Map.of(
                "shortCode", shortCode,
                "originalUrl", url.getOriginalUrl(),
                "totalClicks", totalClicks,
                "clickCount", url.getClickCount(),
                "createdAt", url.getCreatedAt(),
                "recentClicks", recentClicks
        ));
    }

    @GetMapping("/api/urls")
    public ResponseEntity<?> getAllUrls() {
        List<Url> urls = urlRepository.findAll();
        return ResponseEntity.ok(urls);
    }
}