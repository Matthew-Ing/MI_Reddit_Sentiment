package com.mi.backend.reddit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reddit")
public record RedditProperties(
    String baseUrl,
    String host,
    String apiKey,
    int limit,
    int maxRetries
) {}