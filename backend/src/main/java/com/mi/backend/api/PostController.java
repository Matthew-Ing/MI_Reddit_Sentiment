package com.mi.backend.api;

import com.mi.backend.domain.Post;
import com.mi.backend.domain.PostRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

  private final PostRepository posts;

  public PostController(PostRepository posts) {
    this.posts = posts;
  }

  public record PostResponse(
      String redditId, String subreddit, String title, String author,
      int score, int numComments, Instant createdUtc, String permalink,
      String selftextExcerpt, String sentimentLabel, Double sentimentScore,
      String sentimentRationale) {
    static PostResponse from(Post p) {
      return new PostResponse(
          p.getRedditId(), p.getSubreddit(), p.getTitle(), p.getAuthor(),
          p.getScore(), p.getNumComments(), p.getCreatedUtc(), p.getPermalink(),
          p.getSelftextExcerpt(), p.getSentimentLabel(), p.getSentimentScore(),
          p.getSentimentRationale());
    }
  }

  @GetMapping
  public List<PostResponse> byDay(
      @RequestParam String subreddit,
      @RequestParam LocalDate date) {
    Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return posts.findBySubredditAndScoredAtBetween(subreddit, start, end).stream()
        .map(PostResponse::from)
        .toList();
  }
}