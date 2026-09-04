package com.mi.backend.sentiment;

public interface SentimentExtractor {
  SentimentResult classify(String title, String body);
}