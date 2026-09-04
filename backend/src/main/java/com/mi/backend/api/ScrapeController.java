package com.mi.backend.api;

import com.mi.backend.domain.ScrapeRun;
import com.mi.backend.domain.ScrapeRunRepository;
import com.mi.backend.scrape.ScrapeJobPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/scrapes")
public class ScrapeController {

  private final ScrapeJobPublisher publisher;
  private final ScrapeRunRepository runs;

  public ScrapeController(ScrapeJobPublisher publisher, ScrapeRunRepository runs) {
    this.publisher = publisher;
    this.runs = runs;
  }

  public record EnqueueResponse(String status) {}

  public record ScrapeRunResponse(
      Long id, String subreddit, String status,
      Instant startedAt, Instant finishedAt,
      int postsUpserted, int apiCalls, String error) {
    static ScrapeRunResponse from(ScrapeRun r) {
      return new ScrapeRunResponse(
          r.getId(), r.getSubreddit(), r.getStatus().name(),
          r.getStartedAt(), r.getFinishedAt(),
          r.getPostsUpserted(), r.getApiCalls(), r.getError());
    }
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public EnqueueResponse enqueue() {
    publisher.enqueueDaily();
    return new EnqueueResponse("queued");
  }

  @GetMapping
  public List<ScrapeRunResponse> history() {
    return runs.findAllByOrderByStartedAtDesc().stream()
        .map(ScrapeRunResponse::from)
        .toList();
  }
}