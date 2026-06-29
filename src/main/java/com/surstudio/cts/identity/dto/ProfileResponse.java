package com.surstudio.cts.identity.dto;

import java.time.Instant;

public record ProfileResponse(
        Long id,
        String email,
        String displayName,
        String bio,
        Instant updatedAt
) {}