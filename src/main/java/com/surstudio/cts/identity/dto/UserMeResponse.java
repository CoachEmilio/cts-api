package com.surstudio.cts.identity.dto;

public record UserMeResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        String role,
        boolean onboardingComplete,
        String avatarUrl
) {}