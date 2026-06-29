package com.surstudio.cts.onboarding.dto;

import java.util.List;

public record SaveGeneralInfoRequest(List<AnswerEntry> answers) {
    public record AnswerEntry(Long questionId, Long answerId) {}
}