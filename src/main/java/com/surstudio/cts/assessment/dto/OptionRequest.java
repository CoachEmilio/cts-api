package com.surstudio.cts.assessment.dto;

import jakarta.validation.constraints.NotBlank;

public record OptionRequest(
        @NotBlank String text,
        boolean correct,
        int position
) {}