package com.urlshortener.urlshortener.repo;

import com.urlshortener.urlshortener.entities.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortUrl(String shortUrl);

    int deleteByExpiryDateBefore(LocalDateTime now);

    int deleteByCreatedAtBeforeAndExpiryDateIsNull(LocalDateTime localDateTime);
}
