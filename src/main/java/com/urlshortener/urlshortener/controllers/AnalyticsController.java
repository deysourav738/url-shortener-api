package com.urlshortener.urlshortener.controllers;

import com.urlshortener.urlshortener.entities.AnalyticsEntity;
import com.urlshortener.urlshortener.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/{shortUrl}")
    public Map<String, Object> getAnalytics(@PathVariable String shortUrl) {
        List<AnalyticsEntity> records = analyticsService.getAnalyticsForShortUrl(shortUrl);

        // Total Clicks
        long totalClicks = records.size();

        // Geographic Data
        Map<String, Long> geoData = records.stream()
                .collect(Collectors.groupingBy(AnalyticsEntity::getGeoLocation, Collectors.counting()));

        // Referrer Data
        Map<String, Long> referrers = records.stream()
                .collect(Collectors.groupingBy(AnalyticsEntity::getReferrer, Collectors.counting()));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("clicks", totalClicks);
        analytics.put("geoData", geoData);
        analytics.put("referrers", referrers);

        return analytics;
    }
}

