package com.urlshortener.urlshortener.controllers;

import com.urlshortener.urlshortener.exceptions.UrlNotFoundException;
import com.urlshortener.urlshortener.requests.UrlRequest;
import com.urlshortener.urlshortener.responses.UrlResponse;
import com.urlshortener.urlshortener.services.AnalyticsService;
import com.urlshortener.urlshortener.services.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

    @Autowired
    private UrlService urlService;

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody UrlRequest request) {
        logger.info("Received request to shorten URL: {}", request.getLongUrl());

        String shortUrl = urlService.shortenUrl(request.getLongUrl(), request.getExpiryDate());

        logger.info("Shortened URL: {}", shortUrl);
        return ResponseEntity.ok(new UrlResponse(shortUrl));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest request) throws UrlNotFoundException {
        logger.info("Received request for short URL: {}", shortUrl);

        String longUrl = urlService.getLongUrl(shortUrl);

        if (longUrl == null) {
            logger.error("URL not found for: {}", shortUrl);
            throw new UrlNotFoundException("URL not found");
        }

        // Extract client information
        String ipAddress = request.getRemoteAddr();
        String referrer = request.getHeader("Referer");

        logger.info("Redirecting from short URL: {} to long URL: {}", shortUrl, longUrl);
        logger.info("Client IP: {}, Referrer: {}", ipAddress, referrer);

        // Log the access event
        analyticsService.logAccess(shortUrl, ipAddress, referrer);

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }
}
