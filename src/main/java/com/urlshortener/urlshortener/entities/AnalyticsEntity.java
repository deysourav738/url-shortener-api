package com.urlshortener.urlshortener.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics")
@Getter @Setter
public class AnalyticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortUrl;

    @Column(nullable = false)
    private String ipAddress;

    @Column
    private String referrer;

    @Column
    private String geoLocation;

    @Column(nullable = false)
    private LocalDateTime accessTime;
}
