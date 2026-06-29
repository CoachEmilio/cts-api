package com.surstudio.cts.identity.dto;

import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(max = 100) String displayName,
        String bio
) {}