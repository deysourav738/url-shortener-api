package com.urlshortener.urlshortener.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CacheFactory {

    private static final Logger logger = LoggerFactory.getLogger(CacheFactory.class);

    private static volatile Cache cacheInstance;

    @Value("${spring.cache.provider}")
    private String cacheProvider;

    @Autowired
    private ApplicationContext applicationContext;

    public Cache getCache() {
        if (cacheInstance == null) {
            synchronized (CacheFactory.class) {
                if (cacheInstance == null) {
                    logger.info("Initializing cache with provider: {}", cacheProvider);
                    switch (cacheProvider.toLowerCase()) {
                        case "redis":
                            try {
                                cacheInstance = applicationContext.getBean(RedisCache.class);
                                logger.info("Redis cache initialized successfully.");
                            } catch (Exception e) {
                                logger.error("Failed to initialize Redis cache: {}", e.getMessage(), e);
                                throw new RuntimeException("Failed to initialize Redis cache", e);
                            }
                            break;
                        default:
                            logger.error("Invalid cache provider: {}", cacheProvider);
                            throw new IllegalArgumentException("Invalid cache provider: " + cacheProvider);
                    }
                }
            }
        }
        return cacheInstance;
    }
}
