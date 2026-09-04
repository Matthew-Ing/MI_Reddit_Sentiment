package com.mi.backend.reddit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import tools.jackson.databind.json.JsonMapper;

class RedditSmokeTest {

  @Test
  @EnabledIfSystemProperty(named = "reddit.smoke", matches = "true")
  void fetchHotPosts() {
    String apiKey = System.getenv("RAPIDAPI_KEY");
    Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "RAPIDAPI_KEY is not set");

    var props = new RedditProperties(
        "https://reddapi.p.rapidapi.com",
        "reddapi.p.rapidapi.com",
        apiKey,
        10,
        1);
    var client = new RedditApiClient(props, JsonMapper.shared());
    var posts = client.fetchTopDay("EngineeringResumes");
    System.out.println("Fetched " + posts.size() + " posts");
    posts.forEach(p -> System.out.println(p.id() + " | " + p.title()));
  }
}
