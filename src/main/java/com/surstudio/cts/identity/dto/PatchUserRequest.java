package com.surstudio.cts.identity.dto;

import jakarta.validation.constraints.Size;

public record PatchUserRequest(
        Boolean onboardingComplete,
        @Size(max = 500) String avatarUrl,
        @Size(max = 100) String fullName,
        @Size(max = 30)  String phone
) {}
