package com.mi.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "daily_sentiments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"subreddit", "date"})
)
@Getter
@Setter
@NoArgsConstructor
public class DailySentiment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String subreddit;

  @Column(nullable = false)
  private LocalDate date;

  private int postCount;
  private double avgScore;
  private double weightedScore;
  private String label;

  @Column(length = 500)
  private String summary;

  private String model;
  private Instant computedAt;
}