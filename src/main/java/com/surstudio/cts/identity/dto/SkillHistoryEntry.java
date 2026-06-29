package com.surstudio.cts.identity.dto;

import com.surstudio.cts.assessment.domain.Skill;

import java.math.BigDecimal;
import java.time.Instant;

public record SkillHistoryEntry(
        Long attemptId,
        Skill skill,
        String testTitle,
        BigDecimal scorePct,
        int correctCount,
        int totalCount,
        Instant submittedAt
) {}