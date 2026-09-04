package com.mi.backend.sentiment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentiment")
public record SentimentProperties(boolean stub) {
}