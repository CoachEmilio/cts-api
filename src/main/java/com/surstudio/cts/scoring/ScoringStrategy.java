package com.surstudio.cts.scoring;

public interface ScoringStrategy {
    double score(int correctCount, int totalQuestions);
}
