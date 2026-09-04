package com.mi.backend.scrape;

import com.mi.backend.domain.PostRepository;
import com.mi.backend.domain.SubredditRepository;
import com.mi.backend.sentiment.SentimentService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Configuration
public class ScoreExistingRunner {

  @Bean
  @ConditionalOnProperty(name = "sentiment.score-now", havingValue = "true")
  ApplicationRunner scoreExisting(
      SentimentService sentiment,
      SubredditRepository subreddits,
      PostRepository posts) {
    return args -> {
      var day = LocalDate.now(ZoneOffset.UTC);
      for (var sub : subreddits.findByEnabledTrue()) {
        sentiment.scoreAndRollup(sub.getName(), day, posts.findBySubreddit(sub.getName()));
      }
    };
  }
}