package com.mi.backend.scrape;

import com.mi.backend.config.AwsProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ScrapeJobPublisher {

  private static final Logger log = LoggerFactory.getLogger(ScrapeJobPublisher.class);

  private final SqsClient sqs;
  private final String queueUrl;

  public ScrapeJobPublisher(SqsClient sqs, AwsProperties props) {
    this.sqs = sqs;
    this.queueUrl = props.sqs().queueUrl();
  }

  public void enqueueDaily() {
    sqs.sendMessage(b -> b.queueUrl(queueUrl).messageBody("{\"type\":\"DAILY_SCRAPE\"}"));
  }

@Bean
@ConditionalOnProperty(name = "scrape.enqueue-now", havingValue = "true")
ApplicationRunner enqueueNow(ScrapeJobPublisher publisher) {
    return args -> {
        try {
          log.info("Enqueuing scrape job");
          publisher.enqueueDaily();
        } catch (Exception e) {
          log.warn("Could not enqueue scrape job: {}", e.getMessage());
        }
      };
}
}