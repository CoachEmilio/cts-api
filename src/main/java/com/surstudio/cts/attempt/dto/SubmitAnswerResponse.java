package com.surstudio.cts.attempt.dto;

public record SubmitAnswerResponse(
        Long answerId,
        Long questionId,
        Long optionId
) {}
