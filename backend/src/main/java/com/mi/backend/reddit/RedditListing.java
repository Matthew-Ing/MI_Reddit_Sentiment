package com.mi.backend.reddit;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class RedditListing {

  private RedditListing() {}

  public record PostData(
      String id,
      String title,
      String author,
      int score,
      int numComments,
      double createdUtc,
      String permalink,
      String selftext
  ) {
    public Instant createdAt() {
      return Instant.ofEpochSecond((long) createdUtc);
    }

    public String excerpt() {
      if (selftext == null || selftext.isBlank()) {
        return null;
      }
      return selftext.length() <= 2000 ? selftext : selftext.substring(0, 2000);
    }
  }

  static List<PostData> parsePosts(JsonNode root) {
    JsonNode postsNode = findPostsArray(root);
    if (postsNode == null || !postsNode.isArray()) {
      return List.of();
    }
    List<PostData> posts = new ArrayList<>();
    for (JsonNode child : postsNode) {
      JsonNode data = unwrapPost(child);
      if (data == null || !data.isObject()) {
        continue;
      }
      String id = text(data, "id", "post_id", "postId", "name");
      String title = text(data, "title", "post_title");
      if (id == null && title == null) {
        continue;
      }
      posts.add(new PostData(
          id,
          title,
          text(data, "author", "username", "author_name", "authorName"),
          intVal(data, "score", "upvotes", "ups"),
          intVal(data, "num_comments", "numComments", "comments", "comments_count"),
          createdUtc(data),
          text(data, "permalink", "url", "link", "post_url"),
          text(data, "selftext", "content", "text", "body", "self_text")
      ));
    }
    return List.copyOf(posts);
  }

  static String topLevelKeys(JsonNode root) {
    if (root == null || root.isNull() || root.isMissingNode()) {
      return "(null)";
    }
    if (root.isArray()) {
      return "[] size=" + root.size();
    }
    if (!root.isObject()) {
      return root.getNodeType().name();
    }
    List<String> keys = new ArrayList<>();
    root.properties().forEach(e -> keys.add(e.getKey()));
    return keys.toString();
  }

  private static JsonNode findPostsArray(JsonNode node) {
    if (node == null || node.isNull() || node.isMissingNode()) {
      return null;
    }
    if (node.isArray()) {
      return looksLikePosts(node) ? node : null;
    }
    if (!node.isObject()) {
      return null;
    }
    JsonNode children = node.path("data").path("children");
    if (children.isArray()) {
      return children;
    }
    for (String key : List.of("posts", "results", "data", "body", "items")) {
      JsonNode candidate = node.get(key);
      if (candidate == null) {
        continue;
      }
      JsonNode found = findPostsArray(candidate);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static boolean looksLikePosts(JsonNode array) {
    if (array.isEmpty()) {
      return true;
    }
    JsonNode first = unwrapPost(array.get(0));
    if (first == null || !first.isObject()) {
      return false;
    }
    return first.has("title")
        || first.has("id")
        || first.has("selftext")
        || first.has("permalink")
        || first.has("author")
        || first.has("content")
        || first.has("name");
  }

  private static JsonNode unwrapPost(JsonNode node) {
    if (node != null && node.isObject() && node.path("data").isObject()) {
      return node.get("data");
    }
    return node;
  }

  private static String text(JsonNode n, String... keys) {
    for (String key : keys) {
      JsonNode v = n.get(key);
      if (v == null || v.isNull() || v.isMissingNode()) {
        continue;
      }
      if (v.isString() || v.isNumber()) {
        String s = v.asString();
        if (s != null && !s.isBlank() && !"null".equalsIgnoreCase(s)) {
          return s.startsWith("t3_") ? s.substring(3) : s;
        }
      }
      if (v.isObject()) {
        String nested = text(v, "name", "id", "text", "username");
        if (nested != null) {
          return nested;
        }
      }
    }
    return null;
  }

  private static int intVal(JsonNode n, String... keys) {
    for (String key : keys) {
      JsonNode v = n.get(key);
      if (v == null || v.isNull() || v.isMissingNode()) {
        continue;
      }
      if (v.isNumber()) {
        return v.asInt();
      }
      if (v.isString()) {
        try {
          return (int) Double.parseDouble(v.asString().trim());
        } catch (NumberFormatException ignored) {
          // try next key
        }
      }
    }
    return 0;
  }

  private static double createdUtc(JsonNode n) {
    for (String key : List.of("created_utc", "createdUtc", "created", "created_at", "createdAt")) {
      JsonNode v = n.get(key);
      if (v == null || v.isNull() || v.isMissingNode()) {
        continue;
      }
      if (v.isNumber()) {
        return v.asDouble();
      }
      if (v.isString()) {
        String s = v.asString().trim();
        try {
          return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
          // ISO-8601
        }
        try {
          return Instant.parse(s).getEpochSecond();
        } catch (DateTimeParseException ignored) {
          // try next key
        }
      }
    }
    return 0;
  }
}
