package com.surstudio.cts.attempt.dto;

import com.surstudio.cts.attempt.domain.AttemptStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AttemptResultResponse(
        Long attemptId,
        BigDecimal scorePct,
        int correctCount,
        int totalCount,
        AttemptStatus status,
        Instant submittedAt
) {}
