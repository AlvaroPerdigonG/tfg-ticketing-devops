package com.aperdigon.ticketing_backend.application.auth.register;

public record RegisterCommand(
        String email,
        String displayName,
        String password,
        String confirmPassword
) {
}
