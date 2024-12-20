package com.urlshortener.urlshortener.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.urlshortener.entities.AnalyticsEntity;
import com.urlshortener.urlshortener.repo.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "url-access-logs";

    public void logAccess(String shortUrl, String ipAddress, String referrer) {
        AnalyticsEntity analytics = new AnalyticsEntity();
        analytics.setShortUrl(shortUrl);
        analytics.setIpAddress(ipAddress);
        analytics.setReferrer(referrer);
        analytics.setAccessTime(LocalDateTime.now());

        try {
            String logMessage = new ObjectMapper().writeValueAsString(analytics);
            kafkaTemplate.send(TOPIC, logMessage);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing AnalyticsEntity to JSON: " + e);
        }
    }

    public List<AnalyticsEntity> getAnalyticsForShortUrl(String shortUrl) {
        return analyticsRepository.findByShortUrl(shortUrl);
    }

    private String fetchGeoLocation(String ipAddress) {
        String url = "http://ip-api.com/json/" + ipAddress;
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("country") != null) {
                return (String) response.get("country");
            }
        } catch (Exception e) {
            System.err.println("Error fetching geolocation for IP: " + ipAddress);
        }
        return "Unknown";
    }

    @KafkaListener(topics = "url-access-logs", groupId = "analytics-consumers")
    public void consumeLog(String message) {
        try {
            AnalyticsEntity analytics = new ObjectMapper().readValue(message, AnalyticsEntity.class);

            // Fetch geographic data using IP geolocation API
            String geoLocation = fetchGeoLocation(analytics.getIpAddress());

            if (analytics.getReferrer() == null) {
                analytics.setReferrer("Unknown");
            }
            if (geoLocation == null) {
                geoLocation = "Unknown";
            }

            analytics.setGeoLocation(geoLocation);
            analyticsRepository.save(analytics);
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing Kafka message: " + e);
        }
    }
}