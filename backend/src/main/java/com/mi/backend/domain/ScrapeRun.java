package com.mi.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "scrape_runs")
@Getter
@Setter
@NoArgsConstructor
public class ScrapeRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String subreddit;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ScrapeStatus status = ScrapeStatus.RUNNING;

  private Instant startedAt;
  private Instant finishedAt;
  private int postsUpserted;
  private int apiCalls;
  private String error;
}