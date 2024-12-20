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

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UrlController {
    @Autowired
    private UrlService urlService;

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody UrlRequest request) {
        String shortUrl = urlService.shortenUrl(request.getLongUrl(), request.getExpiryDate());
        return ResponseEntity.ok(new UrlResponse(shortUrl));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest request) throws UrlNotFoundException {
        String longUrl = urlService.getLongUrl(shortUrl);

        // Extract client information
        String ipAddress = request.getRemoteAddr();
        String referrer = request.getHeader("Referer");

        // Log the access event
        analyticsService.logAccess(shortUrl, ipAddress, referrer);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }
}