package com.urlshortener.urlshortener.repo;

import com.urlshortener.urlshortener.entities.AnalyticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEntity, Long> {
    List<AnalyticsEntity> findByShortUrl(String shortUrl);
}

