package com.aperdigon.ticketing_backend.api.auth;

import java.util.List;

public record AuthProfileResponse(
        String sub,
        String email,
        String displayName,
        String role,
        List<String> roles
) {
}
