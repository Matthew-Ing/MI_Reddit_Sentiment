package com.mi.backend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
@Profile("local")
public class LocalAwsInitializer {

  private final S3Client s3;
  private final AwsProperties props;

  public LocalAwsInitializer(S3Client s3, AwsProperties props) {
    this.s3 = s3;
    this.props = props;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void createBucket() {
    String bucket = props.s3().bucket();
    try {
      s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
    } catch (Exception e) {
      s3.createBucket(b -> b.bucket(bucket));
    }
  }
}