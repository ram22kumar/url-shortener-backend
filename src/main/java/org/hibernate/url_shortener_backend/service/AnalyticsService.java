package org.hibernate.url_shortener_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.url_shortener_backend.model.ClickEvent;
import org.hibernate.url_shortener_backend.repository.ClickRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickRepository clickRepository;

    @Async
    public void trackClick(String shortCode, String ipAddress, String userAgent, String referrer) {
        try {
            ClickEvent click = ClickEvent.builder()
                    .shortCode(shortCode)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .build();

            clickRepository.save(click);
            log.info("Tracked click for: {} from IP: {}", shortCode, ipAddress);
        } catch (Exception e) {
            log.error("Error tracking click: {}", e.getMessage());
        }
    }
}