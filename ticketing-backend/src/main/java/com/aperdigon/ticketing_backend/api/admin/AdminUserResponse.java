package com.aperdigon.ticketing_backend.api.admin;

import com.aperdigon.ticketing_backend.domain.user.User;

import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String displayName,
        String role,
        boolean isActive
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.id().value(),
                user.email(),
                user.displayName(),
                user.role().name(),
                user.isActive()
        );
    }
}
