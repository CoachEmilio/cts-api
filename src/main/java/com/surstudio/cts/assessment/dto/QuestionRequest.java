package com.surstudio.cts.assessment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuestionRequest(
        @NotBlank String text,
        int position,
        @NotEmpty @Valid List<OptionRequest> options
) {}