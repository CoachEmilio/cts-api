package com.surstudio.cts.onboarding.dto;

import java.util.List;

public record GeneralInfoResponse(
        Long id,
        String name,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            Long id,
            String question,
            String answerType,
            List<OptionDto> answers
    ) {}

    public record OptionDto(Long id, String answer) {}
}