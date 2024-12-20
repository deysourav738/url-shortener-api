package com.urlshortener.urlshortener.services;

import com.urlshortener.urlshortener.repo.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpiryService {
    @Autowired
    private UrlRepository urlRepository;

    @Scheduled(cron = "0 0 * * * ?") // Runs daily at midnight
    public void deleteExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();

        // Remove URLs that have no expiryDate set and have been created more than 5 years ago
        urlRepository.deleteByCreatedAtBeforeAndExpiryDateIsNull(now.minusYears(5));

        // Remove URLs with expiryDate before the current time
        urlRepository.deleteByExpiryDateBefore(now);
    }
}
