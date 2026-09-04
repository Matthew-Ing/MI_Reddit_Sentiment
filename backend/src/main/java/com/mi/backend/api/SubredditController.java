package com.mi.backend.api;

import com.mi.backend.domain.Subreddit;
import com.mi.backend.domain.SubredditRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/subreddits")
public class SubredditController {

  private final SubredditRepository repo;

  public SubredditController(SubredditRepository repo) {
    this.repo = repo;
  }

  public record SubredditResponse(Long id, String name, boolean enabled, Instant lastScrapedAt) {
    static SubredditResponse from(Subreddit s) {
      return new SubredditResponse(s.getId(), s.getName(), s.isEnabled(), s.getLastScrapedAt());
    }
  }

  public record CreateRequest(@NotBlank String name) {}

  public record EnableRequest(boolean enabled) {}

  @GetMapping
  public List<SubredditResponse> list() {
    return repo.findAll().stream().map(SubredditResponse::from).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SubredditResponse add(@Valid @RequestBody CreateRequest body) {
    String name = body.name().replaceFirst("(?i)^r/", "").strip();
    if (repo.findByNameIgnoreCase(name).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Subreddit already exists");
    }
    var s = new Subreddit();
    s.setName(name);
    s.setEnabled(true);
    return SubredditResponse.from(repo.save(s));
  }

  @PatchMapping("/{id}")
  public SubredditResponse enable(@PathVariable Long id, @RequestBody EnableRequest body) {
    var s = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown subreddit"));
    s.setEnabled(body.enabled());
    return SubredditResponse.from(repo.save(s));
  }
}