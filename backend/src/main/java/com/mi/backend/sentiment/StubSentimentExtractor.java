package com.mi.backend.sentiment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sentiment.stub", havingValue = "true", matchIfMissing = true)
public class StubSentimentExtractor implements SentimentExtractor {

  @Override
  public SentimentResult classify(String title, String body) {
    String text = ((title == null ? "" : title) + " " + (body == null ? "" : body))
        .toLowerCase();
    if (text.matches(".*\\b(offer|hired|landed|accepted)\\b.*")) {
      return new SentimentResult(Label.POSITIVE, 0.7, "stub: positive keyword");
    }
    if (text.matches(".*\\b(rejected|unemployed|ghosted|laid off)\\b.*")) {
      return new SentimentResult(Label.NEGATIVE, -0.7, "stub: negative keyword");
    }
    return new SentimentResult(Label.NEUTRAL, 0.0, "stub: no strong signal");
  }
}