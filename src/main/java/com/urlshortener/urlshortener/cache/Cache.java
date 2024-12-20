package com.urlshortener.urlshortener.cache;

public interface Cache {
    void set(String key, String value);
    String get(String key);
    void delete(String key);
    boolean exists(String key);
}

