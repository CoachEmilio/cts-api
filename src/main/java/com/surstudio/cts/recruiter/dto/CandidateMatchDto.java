package com.surstudio.cts.recruiter.dto;

import com.surstudio.cts.assessment.domain.Skill;

import java.math.BigDecimal;
import java.time.Instant;

public record CandidateMatchDto(
        Long userId,
        String displayName,
        Skill skill,
        String testTitle,
        BigDecimal scorePct,
        int correctCount,
        int totalCount,
        Instant submittedAt
) {}