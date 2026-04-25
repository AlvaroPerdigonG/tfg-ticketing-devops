package com.aperdigon.ticketing_backend.application.auth.profile;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.UserRole;

import java.util.List;
import java.util.UUID;

public record GetMyProfileResult(
        UUID userId,
        String email,
        String displayName,
        UserRole role,
        List<String> roles
) {
    public GetMyProfileResult {
        Guard.notNull(userId, "userId");
        Guard.notBlank(email, "email");
        Guard.notBlank(displayName, "displayName");
        Guard.notNull(role, "role");
        roles = List.copyOf(Guard.notNull(roles, "roles"));
    }
}
