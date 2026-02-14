package com.aperdigon.ticketing_backend.domain.category;


import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public final class Category {
    private final CategoryId id;
    private final String name;
    private final boolean isActive;

    public Category(CategoryId id, String name, boolean isActive) {
        this.id = Guard.notNull(id, "id");
        this.name = Guard.notBlank(name, "name");
        this.isActive = isActive;
    }

    public CategoryId id() { return id; }
    public String name() { return name; }
    public boolean isActive() { return isActive; }
}
