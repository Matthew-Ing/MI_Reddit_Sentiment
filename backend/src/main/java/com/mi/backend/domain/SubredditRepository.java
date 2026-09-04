package com.mi.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Long> {
  Optional<Subreddit> findByNameIgnoreCase(String name);
  List<Subreddit> findByEnabledTrue();
}