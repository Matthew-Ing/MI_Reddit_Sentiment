package com.mi.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySentimentRepository extends JpaRepository<DailySentiment, Long> {
  Optional<DailySentiment> findBySubredditAndDate(String subreddit, LocalDate date);
  List<DailySentiment> findBySubredditAndDateBetweenOrderByDateAsc(
      String subreddit, LocalDate from, LocalDate to);
}