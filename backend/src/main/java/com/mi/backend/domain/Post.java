package com.mi.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String redditId;

  @Column(nullable = false)
  private String subreddit;

  private String title;
  private String author;
  private int score;
  private int numComments;
  private Instant createdUtc;
  private String permalink;

  @Column(length = 2000)
  private String selftextExcerpt;

  private String s3Key;

  private String sentimentLabel;
  private Double sentimentScore;
  private String sentimentRationale;
  private String sentimentModel;
  private Instant scoredAt;
}