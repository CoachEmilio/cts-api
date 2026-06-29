package com.surstudio.cts.identity.dto;

public record AuthResponse(String token, String role, boolean onboardingComplete) {}
