package com.mi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsConfig {

  @Bean
  S3Client s3Client(AwsProperties props) {
    return S3Client.builder()
        .endpointOverride(URI.create(props.endpoint()))
        .region(Region.of(props.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")))
        .forcePathStyle(true)
        .build();
  }

  @Bean
SqsClient sqsClient(AwsProperties props) {
  return SqsClient.builder()
      .endpointOverride(URI.create(props.endpoint()))
      .region(Region.of(props.region()))
      .credentialsProvider(
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create("test", "test")))
      .build();
}
}