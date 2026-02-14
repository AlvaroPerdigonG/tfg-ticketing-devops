package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper;

import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;

public final class UserMapper {
    private UserMapper() {}

    public static User toDomain(UserJpaEntity e) {
        return new User(
                new UserId(e.getId()),
                e.getEmail(),
                e.getDisplayName(),
                e.getRole(),
                e.isActive()
        );
    }
}
