package com.mi.backend.reddit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Component
public class RedditApiClient {

  private static final Logger log = LoggerFactory.getLogger(RedditApiClient.class);

  private final RestClient rest;
  private final RedditProperties props;
  private final JsonMapper mapper;

  public RedditApiClient(RedditProperties props, JsonMapper mapper) {
    this.props = props;
    this.mapper = mapper;
    this.rest = RestClient.builder()
        .baseUrl(props.baseUrl())
        .defaultHeader("X-RapidAPI-Key", props.apiKey())
        .defaultHeader("X-RapidAPI-Host", props.host())
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  public List<RedditListing.PostData> fetchTopDay(String subreddit) {
    RestClientResponseException lastHttp = null;
    for (int attempt = 1; attempt <= props.maxRetries(); attempt++) {
      try {
        String body = rest.get()
            .uri(uri -> uri.path("/api/scrape/hot")
                .queryParam("subreddit", subreddit)
                .queryParam("limit", props.limit())
                .build())
            .retrieve()
            .body(String.class);

        if (body == null || body.isBlank() || looksLikeHtml(body)) {
          log.warn("ReddAPI returned empty/HTML for r/{} (attempt {})", subreddit, attempt);
          sleep(attempt);
          continue;
        }

        JsonNode root = mapper.readTree(body);
        List<RedditListing.PostData> posts = RedditListing.parsePosts(root);
        if (posts.isEmpty()) {
          log.warn("ReddAPI parsed 0 posts for r/{}; keys={} snippet={}",
              subreddit, RedditListing.topLevelKeys(root), snippet(body));
        }
        return posts;
      } catch (RestClientResponseException e) {
        lastHttp = e;
        int status = e.getStatusCode().value();
        if (status != 403 && status != 429 && status != 503) {
          throw e;
        }
        sleepUntilReset(e, attempt);
      } catch (Exception e) {
        if (attempt == props.maxRetries()) {
          throw new IllegalStateException("Failed to parse Reddit listing for r/" + subreddit, e);
        }
        sleep(attempt);
      }
    }
    if (lastHttp != null) {
      throw lastHttp;
    }
    throw new IllegalStateException("Reddit returned empty or HTML for r/" + subreddit);
  }

  private static String snippet(String body) {
    String compact = body.replaceAll("\\s+", " ").strip();
    return compact.length() <= 400 ? compact : compact.substring(0, 400);
  }

  private static boolean looksLikeHtml(String body) {
    String trimmed = body.stripLeading();
    return trimmed.startsWith("<") || trimmed.toLowerCase().contains("<html");
  }

  private void sleepUntilReset(RestClientResponseException e, int attempt) {
    String retryAfter = e.getResponseHeaders() != null
        ? e.getResponseHeaders().getFirst("Retry-After")
        : null;
    long seconds = 2L * attempt;
    if (retryAfter != null) {
      try {
        seconds = Math.max(1, Long.parseLong(retryAfter.trim()));
      } catch (NumberFormatException ignored) {
        // keep backoff
      }
    }
    sleepSeconds(seconds);
  }

  private void sleep(int attempt) {
    sleepSeconds(2L * attempt);
  }

  private static void sleepSeconds(long seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted waiting on Reddit retry", ie);
    }
  }
}
