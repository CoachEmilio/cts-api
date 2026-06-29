package com.surstudio.cts.scoring;

import org.springframework.stereotype.Component;

// score = correct / total * 100, rounded to 2 decimal places.
// Lives behind ScoringStrategy so alternate rules can be added without touching AttemptService.
@Component
public class FlatScoringStrategy implements ScoringStrategy {

    @Override
    public double score(int correctCount, int totalQuestions) {
        if (totalQuestions == 0) return 0.0;
        return Math.round((double) correctCount / totalQuestions * 10_000.0) / 100.0;
    }
}
