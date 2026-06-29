package com.surstudio.cts.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        String fullName,
        @Email @NotBlank String email,
        String phone,
        @NotBlank @Size(min = 8) String password
) {}