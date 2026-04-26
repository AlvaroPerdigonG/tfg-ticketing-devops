package com.aperdigon.ticketing_backend.application.admin.users.update_active;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.UserId;

public record UpdateUserActiveCommand(UserId userId, boolean isActive) {
    public UpdateUserActiveCommand {
        Guard.notNull(userId, "userId");
    }
}
