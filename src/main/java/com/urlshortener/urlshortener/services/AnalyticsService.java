package com.urlshortener.urlshortener.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.urlshortener.entities.AnalyticsEntity;
import com.urlshortener.urlshortener.repo.AnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "url-access-logs";

    public void logAccess(String shortUrl, String ipAddress, String referrer) {
        logger.info("Logging access for short URL: {} from IP: {} with referrer: {}", shortUrl, ipAddress, referrer);

        AnalyticsEntity analytics = new AnalyticsEntity();
        analytics.setShortUrl(shortUrl);
        analytics.setIpAddress(ipAddress);
        analytics.setReferrer(referrer);
        analytics.setAccessTime(LocalDateTime.now());

        try {
            String logMessage = new ObjectMapper().writeValueAsString(analytics);
            kafkaTemplate.send(TOPIC, logMessage);
            logger.info("Access log sent to Kafka for short URL: {}", shortUrl);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing AnalyticsEntity to JSON for short URL: {}", shortUrl, e);
        }
    }

    public List<AnalyticsEntity> getAnalyticsForShortUrl(String shortUrl) {
        logger.info("Fetching analytics for short URL: {}", shortUrl);
        return analyticsRepository.findByShortUrl(shortUrl);
    }

    private String fetchGeoLocation(String ipAddress) {
        logger.info("Fetching geolocation for IP address: {}", ipAddress);

        String url = "http://ip-api.com/json/" + ipAddress;
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("country") != null) {
                String geoLocation = (String) response.get("country");
                logger.info("Geolocation fetched for IP {}: {}", ipAddress, geoLocation);
                return geoLocation;
            }
        } catch (Exception e) {
            logger.error("Error fetching geolocation for IP address: {}", ipAddress, e);
        }
        return "Unknown";
    }

    @KafkaListener(topics = "url-access-logs", groupId = "analytics-consumers")
    public void consumeLog(String message) {
        logger.info("Consuming message from Kafka: {}", message);

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

            logger.info("Access log saved for short URL: {} with geolocation: {}", analytics.getShortUrl(), geoLocation);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing Kafka message: {}", message, e);
        }
    }
}
