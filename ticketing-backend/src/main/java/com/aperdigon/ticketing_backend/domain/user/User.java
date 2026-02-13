package com.aperdigon.ticketing_backend.domain.user;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public final class User {
    private final UserId id;
    private final String email;
    private final String displayName;
    private final UserRole role;
    private final boolean isActive;

    public User(UserId id, String email, String displayName, UserRole role, boolean isActive) {
        this.id = Guard.notNull(id, "id");
        this.email = Guard.notBlank(email, "email");
        this.displayName = Guard.notBlank(displayName, "displayName");
        this.role = Guard.notNull(role, "role");
        this.isActive = isActive;
    }

    public UserId id() { return id; }
    public String email() { return email; }
    public String displayName() { return displayName; }
    public UserRole role() { return role; }
    public boolean isActive() { return isActive; }

    public boolean isAgentOrAdmin() {
        return role == UserRole.AGENT || role == UserRole.ADMIN;
    }
}
