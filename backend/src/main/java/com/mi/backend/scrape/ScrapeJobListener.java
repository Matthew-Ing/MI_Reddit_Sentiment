package com.mi.backend.scrape;

import com.mi.backend.config.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

@Component
public class ScrapeJobListener {

  private static final Logger log = LoggerFactory.getLogger(ScrapeJobListener.class);

  private final SqsClient sqs;
  private final String queueUrl;
  private final ScrapeOrchestrator orchestrator;

  public ScrapeJobListener(SqsClient sqs, AwsProperties props, ScrapeOrchestrator orchestrator) {
    this.sqs = sqs;
    this.queueUrl = props.sqs().queueUrl();
    this.orchestrator = orchestrator;
  }

  @Scheduled(fixedDelay = 5000)
  public void poll() {
    var response = sqs.receiveMessage(b -> b
        .queueUrl(queueUrl)
        .maxNumberOfMessages(1)
        .waitTimeSeconds(5));
    for (Message message : response.messages()) {
      try {
        log.info("Received scrape job: {}", message.body());
        orchestrator.runDaily();
        sqs.deleteMessage(b -> b.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
      } catch (Exception e) {
        log.warn("Job failed, leaving message on queue: {}", e.getMessage());
      }
    }
  }
}