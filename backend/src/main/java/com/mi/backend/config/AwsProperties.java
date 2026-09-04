package com.mi.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
    String endpoint,
    String region,
    S3 s3
) {
  public record S3(String bucket) {}
}