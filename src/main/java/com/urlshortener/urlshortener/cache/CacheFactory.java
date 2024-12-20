package com.urlshortener.urlshortener.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CacheFactory {

    private static volatile Cache cacheInstance;

    @Value("${spring.cache.provider}")
    private String cacheProvider;

    @Autowired
    private ApplicationContext applicationContext;

    public Cache getCache() {
        if (cacheInstance == null) {
            synchronized (CacheFactory.class) {
                if (cacheInstance == null) {
                    switch (cacheProvider.toLowerCase()) {
                        case "redis":
                            try{
                                cacheInstance = applicationContext.getBean(RedisCache.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid cache provider");
                    }
                }
            }
        }
        return cacheInstance;
    }
}
