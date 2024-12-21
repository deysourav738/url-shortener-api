package com.urlshortener.urlshortener.services;

import com.urlshortener.urlshortener.cache.Cache;
import com.urlshortener.urlshortener.cache.CacheFactory;
import com.urlshortener.urlshortener.entities.UrlEntity;
import com.urlshortener.urlshortener.exceptions.UrlNotFoundException;
import com.urlshortener.urlshortener.repo.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CacheFactory cacheFactory;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String getLongUrl(String shortUrl) throws UrlNotFoundException {
        logger.info("Attempting to retrieve long URL for short URL: {}", shortUrl);

        Cache cache = cacheFactory.getCache();
        // Check in the cache first
        String longUrl = cache.get(shortUrl);
        if (longUrl == null) {
            logger.info("Cache miss for short URL: {}, querying database", shortUrl);
            // Fallback to database
            UrlEntity urlEntity = urlRepository.findByShortUrl(shortUrl)
                    .orElseThrow(() -> {
                        logger.error("URL not found for short URL: {}", shortUrl);
                        return new UrlNotFoundException("URL not found");
                    });
            longUrl = urlEntity.getLongUrl();
            // Store in cache for future requests
            cache.set(shortUrl, longUrl);
            logger.info("URL found in database, caching for future use. Long URL: {}", longUrl);
        } else {
            logger.info("Cache hit for short URL: {}", shortUrl);
        }
        return longUrl;
    }

    public void saveUrl(String shortUrl, String longUrl, LocalDateTime expiryDate) {
        logger.info("Saving short URL: {} with long URL: {} and expiry date: {}", shortUrl, longUrl, expiryDate);

        // Save to both database and cache
        Cache cache = cacheFactory.getCache();
        cache.set(shortUrl, longUrl);

        UrlEntity urlEntity = new UrlEntity();
        urlEntity.setLongUrl(longUrl);
        urlEntity.setShortUrl(shortUrl);
        urlEntity.setExpiryDate(expiryDate);

        urlRepository.save(urlEntity);
        logger.info("Short URL: {} saved successfully in database and cache", shortUrl);
    }

    public String shortenUrl(String longUrl, LocalDateTime expiryDate) {
        logger.info("Shortening long URL: {} with expiry date: {}", longUrl, expiryDate);

        // Generate a unique short URL
        String shortUrl = generateShortUrl();

        // Save it to the database and cache
        saveUrl(shortUrl, longUrl, expiryDate);

        logger.info("Generated short URL: {} for long URL: {}", shortUrl, longUrl);
        return shortUrl;
    }

    private String generateShortUrl() {
        // Generate a 6-character random string for the short URL
        StringBuilder shortUrl = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(ALPHABET.length());
            shortUrl.append(ALPHABET.charAt(index));
        }
        return shortUrl.toString();
    }
}
