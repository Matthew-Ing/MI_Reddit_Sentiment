package com.mi.backend.sentiment;

import com.mi.backend.domain.DailySentiment;
import com.mi.backend.domain.DailySentimentRepository;
import com.mi.backend.domain.Post;
import com.mi.backend.domain.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class SentimentService {

  private static final Logger log = LoggerFactory.getLogger(SentimentService.class);

  private final SentimentExtractor extractor;
  private final PostRepository posts;
  private final DailySentimentRepository daily;

  public SentimentService(
      SentimentExtractor extractor,
      PostRepository posts,
      DailySentimentRepository daily) {
    this.extractor = extractor;
    this.posts = posts;
    this.daily = daily;
  }

  public void scoreAndRollup(String subreddit, LocalDate date, List<Post> batch) {
    if (batch == null || batch.isEmpty()) {
      log.info("No posts to score for r/{}", subreddit);
      return;
    }
    for (Post post : batch) {
      SentimentResult r = extractor.classify(post.getTitle(), post.getSelftextExcerpt());
      post.setSentimentLabel(r.label().name());
      post.setSentimentScore(r.score());
      post.setSentimentRationale(r.rationale());
      post.setSentimentModel("stub");
      post.setScoredAt(Instant.now());
      posts.save(post);
    }
    upsertDaily(subreddit, date, batch);
    log.info("Scored {} posts for r/{} on {}", batch.size(), subreddit, date);
  }

  private void upsertDaily(String subreddit, LocalDate date, List<Post> batch) {
    double weightSum = 0;
    double weighted = 0;
    double simple = 0;
    for (Post p : batch) {
      double s = p.getSentimentScore() == null ? 0 : p.getSentimentScore();
      int w = Math.max(p.getScore(), 0);
      simple += s;
      weighted += s * w;
      weightSum += w;
    }
    double avg = simple / batch.size();
    double wAvg = weightSum == 0 ? avg : weighted / weightSum;
    String label = wAvg > 0.15 ? "POSITIVE" : wAvg < -0.15 ? "NEGATIVE" : "NEUTRAL";

    DailySentiment row = daily.findBySubredditAndDate(subreddit, date)
        .orElseGet(DailySentiment::new);
    row.setSubreddit(subreddit);
    row.setDate(date);
    row.setPostCount(batch.size());
    row.setAvgScore(avg);
    row.setWeightedScore(wAvg);
    row.setLabel(label);
    row.setSummary("stub: %d posts, weighted %.2f".formatted(batch.size(), wAvg));
    row.setModel("stub");
    row.setComputedAt(Instant.now());
    daily.save(row);
  }
}