package com.aperdigon.ticketing_backend.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String displayName,
        @NotBlank String password,
        @NotBlank String confirmPassword
) {
}
