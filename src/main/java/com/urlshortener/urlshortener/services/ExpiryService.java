package com.urlshortener.urlshortener.services;

import com.urlshortener.urlshortener.repo.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpiryService {

    private static final Logger logger = LoggerFactory.getLogger(ExpiryService.class);

    @Autowired
    private UrlRepository urlRepository;

    @Scheduled(cron = "0 0 * * * ?") // Runs daily at midnight
    public void deleteExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Starting expired URL cleanup task at {}", now);

        try {
            // Remove URLs that have no expiryDate set and have been created more than 5 years ago
            int deletedOldUrls = urlRepository.deleteByCreatedAtBeforeAndExpiryDateIsNull(now.minusYears(5));
            logger.info("Deleted {} URLs created more than 5 years ago without an expiry date", deletedOldUrls);

            // Remove URLs with expiryDate before the current time
            int deletedExpiredUrls = urlRepository.deleteByExpiryDateBefore(now);
            logger.info("Deleted {} URLs with expiry dates before {}", deletedExpiredUrls, now);

        } catch (Exception e) {
            logger.error("An error occurred while deleting expired URLs: {}", e.getMessage(), e);
        }

        logger.info("Expired URL cleanup task completed at {}", LocalDateTime.now());
    }
}
