package com.mi.backend.api;

import com.mi.backend.domain.DailySentiment;
import com.mi.backend.domain.DailySentimentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sentiment/daily")
public class DailySentimentController {

  private final DailySentimentRepository daily;

  public DailySentimentController(DailySentimentRepository daily) {
    this.daily = daily;
  }

  public record DailyResponse(
      String subreddit, LocalDate date, int postCount,
      double avgScore, double weightedScore, String label,
      String summary, String model, Instant computedAt) {
    static DailyResponse from(DailySentiment d) {
      return new DailyResponse(
          d.getSubreddit(), d.getDate(), d.getPostCount(),
          d.getAvgScore(), d.getWeightedScore(), d.getLabel(),
          d.getSummary(), d.getModel(), d.getComputedAt());
    }
  }

  @GetMapping
  public List<DailyResponse> history(
      @RequestParam String subreddit,
      @RequestParam LocalDate from,
      @RequestParam LocalDate to) {
    return daily.findBySubredditAndDateBetweenOrderByDateAsc(subreddit, from, to)
        .stream()
        .map(DailyResponse::from)
        .toList();
  }
}