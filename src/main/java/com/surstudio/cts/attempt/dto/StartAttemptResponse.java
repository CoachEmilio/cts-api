package com.surstudio.cts.attempt.dto;

import com.surstudio.cts.attempt.domain.AttemptStatus;

import java.time.Instant;

public record StartAttemptResponse(
        Long attemptId,
        Long testId,
        AttemptStatus status,
        Instant startedAt,
        Instant deadline
) {}
