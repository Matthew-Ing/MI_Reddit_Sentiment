package com.mi.backend.sentiment;

public record SentimentResult(Label label, double score, String rationale) {}