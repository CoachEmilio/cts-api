package com.surstudio.cts.assessment.dto;

import com.surstudio.cts.assessment.domain.Skill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SkillTestRequest(
        @NotNull Skill skill,
        @NotBlank String title,
        Boolean active
) {}