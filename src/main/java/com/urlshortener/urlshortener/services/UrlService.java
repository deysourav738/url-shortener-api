package com.urlshortener.urlshortener.services;

import com.urlshortener.urlshortener.cache.Cache;
import com.urlshortener.urlshortener.cache.CacheFactory;
import com.urlshortener.urlshortener.entities.UrlEntity;
import com.urlshortener.urlshortener.exceptions.UrlNotFoundException;
import com.urlshortener.urlshortener.repo.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CacheFactory cacheFactory;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String getLongUrl(String shortUrl) throws UrlNotFoundException {
        Cache cache = cacheFactory.getCache();
        // Check in the cache first
        String longUrl = cache.get(shortUrl);
        if (longUrl == null) {
            // Fallback to database
            UrlEntity urlEntity = urlRepository.findByShortUrl(shortUrl)
                    .orElseThrow(() -> new UrlNotFoundException("URL not found"));
            longUrl = urlEntity.getLongUrl();
            // Store in cache for future requests
            cache.set(shortUrl, longUrl);
        }
        return longUrl;
    }

    public void saveUrl(String shortUrl, String longUrl, LocalDateTime expiryDate) {
        // Save to both database and cache
        Cache cache = cacheFactory.getCache();
        cache.set(shortUrl, longUrl);
        UrlEntity urlEntity = new UrlEntity();
        urlEntity.setLongUrl(longUrl);
        urlEntity.setShortUrl(shortUrl);
        urlEntity.setExpiryDate(expiryDate);
        urlRepository.save(urlEntity);
    }

    public String shortenUrl(String longUrl, LocalDateTime expiryDate) {
        // Generate a unique short URL
        String shortUrl = generateShortUrl();

        // Save it to the database and cache
        saveUrl(shortUrl, longUrl, expiryDate);

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