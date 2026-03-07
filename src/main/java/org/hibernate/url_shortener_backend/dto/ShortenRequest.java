package org.hibernate.url_shortener_backend.dto;

import java.time.LocalDateTime;

public class ShortenRequest {
    private String url;
    private String customAlias;
    private LocalDateTime expiresAt;

    // Constructors
    public ShortenRequest() {
    }

    public ShortenRequest(String url, String customAlias, LocalDateTime expiresAt) {
        this.url = url;
        this.customAlias = customAlias;
        this.expiresAt = expiresAt;
    }

    // Getters
    public String getUrl() {
        return url;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    // Setters
    public void setUrl(String url) {
        this.url = url;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}