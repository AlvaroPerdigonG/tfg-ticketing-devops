package com.aperdigon.ticketing_backend.domain.user;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public final class User {
    private final UserId id;
    private final String email;
    private final String displayName;
    private final String passwordHash;
    private final UserRole role;
    private final boolean isActive;

    public User(UserId id, String email, String displayName, String passwordHash, UserRole role, boolean isActive) {
        this.id = Guard.notNull(id, "id");
        this.email = Guard.notBlank(email, "email");
        this.displayName = Guard.notBlank(displayName, "displayName");
        this.passwordHash = validatePasswordHash(passwordHash);
        this.role = Guard.notNull(role, "role");
        this.isActive = isActive;
    }

    private String validatePasswordHash(String passwordHash) {
        String hash = Guard.notBlank(passwordHash, "passwordHash");
        if (hash.length() < 40) {
            throw new IllegalArgumentException("passwordHash must look like a secure hash");
        }
        return hash;
    }

    public UserId id() { return id; }
    public String email() { return email; }
    public String displayName() { return displayName; }
    public String passwordHash() { return passwordHash; }
    public UserRole role() { return role; }
    public boolean isActive() { return isActive; }

    public boolean isAgentOrAdmin() {
        return role == UserRole.AGENT || role == UserRole.ADMIN;
    }
}
