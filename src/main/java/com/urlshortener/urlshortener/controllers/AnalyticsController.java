package com.urlshortener.urlshortener.controllers;

import com.urlshortener.urlshortener.entities.AnalyticsEntity;
import com.urlshortener.urlshortener.services.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/analytics")
public class AnalyticsController {

    // Create a Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/{shortUrl}")
    public Map<String, Object> getAnalytics(@PathVariable String shortUrl) {
        logger.info("Fetching analytics for short URL: {}", shortUrl);

        // Fetch the analytics records for the provided short URL
        List<AnalyticsEntity> records = analyticsService.getAnalyticsForShortUrl(shortUrl);
        logger.debug("Found {} records for short URL: {}", records.size(), shortUrl);

        // Total Clicks
        long totalClicks = records.size();
        logger.debug("Total clicks for short URL {}: {}", shortUrl, totalClicks);

        // Geographic Data
        Map<String, Long> geoData = records.stream()
                .collect(Collectors.groupingBy(AnalyticsEntity::getGeoLocation, Collectors.counting()));
        logger.debug("Geographic data: {}", geoData);

        // Referrer Data
        Map<String, Long> referrers = records.stream()
                .collect(Collectors.groupingBy(AnalyticsEntity::getReferrer, Collectors.counting()));
        logger.debug("Referrer data: {}", referrers);

        // Prepare the response map
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("clicks", totalClicks);
        analytics.put("geoData", geoData);
        analytics.put("referrers", referrers);

        logger.info("Returning analytics for short URL: {}", shortUrl);
        return analytics;
    }
}
