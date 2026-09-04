package com.mi.backend.config;

// import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
// import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import jakarta.annotation.PostConstruct;


@Component
@Profile("local")
public class LocalAwsInitializer {

  private final S3Client s3;
  private final SqsClient sqs;
  private final AwsProperties props;

  public LocalAwsInitializer(S3Client s3, SqsClient sqs, AwsProperties props) {
    this.s3 = s3;
    this.sqs = sqs;
    this.props = props;
  }

  // @EventListener(ApplicationReadyEvent.class)
  @PostConstruct
  public void init() {
    String bucket = props.s3().bucket();
    try {
      s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
    } catch (Exception e) {
      s3.createBucket(b -> b.bucket(bucket));
    }
    String url;
    try {
      url =sqs.getQueueUrl(b -> b.queueName("scrape-jobs")).queueUrl();
    } catch (QueueDoesNotExistException e) {
      url =sqs.createQueue(b -> b.queueName("scrape-jobs")).queueUrl();
    }
    // log.info("Queue URL: {}", url);
  }
}