package org.hibernate.url_shortener_backend.repository;

import org.hibernate.url_shortener_backend.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClickRepository extends JpaRepository<ClickEvent, Long> {

    long countByShortCode(String shortCode);

    List<ClickEvent> findByShortCodeOrderByClickedAtDesc(String shortCode);

    @Query("SELECT FUNCTION('DATE', c.clickedAt) as date, COUNT(c) as count " +
            "FROM ClickEvent c WHERE c.shortCode = ?1 " +
            "GROUP BY FUNCTION('DATE', c.clickedAt) " +
            "ORDER BY date DESC")
    List<Object[]> getClicksByDay(String shortCode);
}