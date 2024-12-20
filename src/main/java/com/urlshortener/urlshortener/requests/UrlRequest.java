package com.urlshortener.urlshortener.requests;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlRequest {
    private String longUrl;
    private LocalDateTime expiryDate;
}
