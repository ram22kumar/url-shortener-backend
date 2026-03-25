package org.hibernate.url_shortener_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String shortCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Long clickCount;

    private String userId;

    @Column(unique = true, length = 50)
    private String customAlias;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (clickCount == null) {
            clickCount = 0L;
        }
    }
}