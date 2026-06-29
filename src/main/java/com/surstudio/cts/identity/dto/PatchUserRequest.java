package com.surstudio.cts.identity.dto;

public record PatchUserRequest(
        Boolean onboardingComplete,
        String avatarUrl,
        String fullName,
        String phone
) {}