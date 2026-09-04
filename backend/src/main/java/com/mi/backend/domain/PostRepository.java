package com.mi.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  Optional<Post> findByRedditId(String redditId);
  List<Post> findBySubredditAndCreatedUtcBetween(
      String subreddit, Instant start, Instant end);
}