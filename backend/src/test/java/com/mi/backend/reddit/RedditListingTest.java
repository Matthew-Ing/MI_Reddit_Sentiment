package com.mi.backend.reddit;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedditListingTest {

  private final JsonMapper mapper = JsonMapper.shared();

  @Test
  void parsesOfficialRedditListing() {
    var posts = parse("""
        {"data":{"children":[{"data":{"id":"abc","title":"Hello","author":"bob",
        "score":12,"num_comments":3,"created_utc":1700000000,
        "permalink":"/r/x/comments/abc/hello/","selftext":"body"}}]}}
        """);
    assertEquals(1, posts.size());
    assertEquals("abc", posts.get(0).id());
    assertEquals("Hello", posts.get(0).title());
    assertEquals("bob", posts.get(0).author());
    assertEquals(12, posts.get(0).score());
    assertEquals(3, posts.get(0).numComments());
    assertEquals("body", posts.get(0).selftext());
  }

  @Test
  void parsesBodyArrayWithAliases() {
    var posts = parse("""
        {"body":[{"id":"xyz","title":"Resume help","username":"ann",
        "upvotes":4,"comments":1,"created":"2024-01-15T10:30:00Z",
        "url":"https://reddit.com/r/x/comments/xyz","content":"please review"}]}
        """);
    assertEquals(1, posts.size());
    RedditListing.PostData p = posts.get(0);
    assertEquals("xyz", p.id());
    assertEquals("Resume help", p.title());
    assertEquals("ann", p.author());
    assertEquals(4, p.score());
    assertEquals(1, p.numComments());
    assertEquals("please review", p.selftext());
    assertTrue(p.createdUtc() > 0);
  }

  @Test
  void parsesNestedBodyListing() {
    var posts = parse("""
        {"body":{"kind":"Listing","data":{"children":[{"data":{"id":"n1","title":"Nested"}}]}}}
        """);
    assertEquals("n1", posts.get(0).id());
  }

  @Test
  void parsesRootArrayAndPostsKey() {
    assertEquals("a1", parse("[{\"id\":\"a1\",\"title\":\"A\"}]").get(0).id());
    assertEquals("p1", parse("{\"posts\":[{\"id\":\"p1\",\"title\":\"P\"}]}").get(0).id());
    assertEquals("d1", parse("{\"data\":[{\"id\":\"d1\",\"title\":\"D\"}]}").get(0).id());
  }

  @Test
  void emptyWhenWrapperHasNoPosts() {
    assertTrue(parse("{\"message\":\"ok\",\"status\":200}").isEmpty());
  }

  private List<RedditListing.PostData> parse(String json) {
    return RedditListing.parsePosts(mapper.readTree(json));
  }
}
