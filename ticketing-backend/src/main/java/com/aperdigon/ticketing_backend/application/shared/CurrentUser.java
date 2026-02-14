package com.aperdigon.ticketing_backend.application.shared;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;

public record CurrentUser(UserId id, UserRole role) {
    public CurrentUser {
        Guard.notNull(id, "id");
        Guard.notNull(role, "role");
    }

    public boolean isAgentOrAdmin() {
        return role == UserRole.AGENT || role == UserRole.ADMIN;
    }
}
