package com.surstudio.cts.assessment.dto;

import com.surstudio.cts.assessment.domain.Skill;

import java.util.List;

public record SkillTestAdminResponse(
        Long id,
        Skill skill,
        String title,
        boolean active,
        int durationMinutes,
        List<QuestionDto> questions
) {
    public record QuestionDto(Long id, String text, int position, List<OptionDto> options) {}
    public record OptionDto(Long id, String text, boolean correct, int position) {}
}