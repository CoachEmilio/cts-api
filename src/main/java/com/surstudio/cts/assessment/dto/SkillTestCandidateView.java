package com.surstudio.cts.assessment.dto;

import com.surstudio.cts.assessment.domain.Skill;

import java.util.List;

// Trust boundary: isCorrect is intentionally absent from every level of this response.
public record SkillTestCandidateView(
        Long id,
        Skill skill,
        String title,
        List<QuestionDto> questions
) {
    public record QuestionDto(Long id, String text, int position, List<OptionDto> options) {}
    public record OptionDto(Long id, String text, int position) {}
}