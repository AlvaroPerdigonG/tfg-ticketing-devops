package com.aperdigon.ticketing_backend.test_support.builders;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public final class UserTestDataBuilder {
    private UUID id = UUID.randomUUID();
    private String email = "user@test.com";
    private String displayName = "User";
    private String rawPassword = "secret123";
    private UserRole role = UserRole.USER;
    private boolean active = true;

    private UserTestDataBuilder() {
    }

    public static UserTestDataBuilder aUser() {
        return new UserTestDataBuilder();
    }

    public UserTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public UserTestDataBuilder withPassword(String rawPassword) {
        this.rawPassword = rawPassword;
        return this;
    }

    public UserTestDataBuilder withRole(UserRole role) {
        this.role = role;
        return this;
    }

    public UserTestDataBuilder active() {
        this.active = true;
        return this;
    }

    public UserTestDataBuilder inactive() {
        this.active = false;
        return this;
    }

    public UserJpaEntity build(PasswordEncoder passwordEncoder) {
        return new UserJpaEntity(
                id,
                email,
                displayName,
                passwordEncoder.encode(rawPassword),
                role,
                active
        );
    }
}
