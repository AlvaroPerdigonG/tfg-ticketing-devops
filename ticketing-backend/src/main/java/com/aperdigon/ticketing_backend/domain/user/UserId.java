package com.aperdigon.ticketing_backend.domain.user;

import java.util.UUID;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public record UserId(UUID value) {
    public UserId {
        Guard.notNull(value, "userId");
    }

    public static UserId of(UUID value) { return new UserId(value); }
}
