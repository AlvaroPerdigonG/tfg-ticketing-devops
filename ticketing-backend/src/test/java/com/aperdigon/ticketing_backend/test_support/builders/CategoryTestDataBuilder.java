package com.aperdigon.ticketing_backend.test_support.builders;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;

import java.util.UUID;

public final class CategoryTestDataBuilder {
    private UUID id = UUID.randomUUID();
    private String name = "General";
    private boolean active = true;

    private CategoryTestDataBuilder() {
    }

    public static CategoryTestDataBuilder aCategory() {
        return new CategoryTestDataBuilder();
    }

    public CategoryTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public CategoryTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryTestDataBuilder active() {
        this.active = true;
        return this;
    }

    public CategoryTestDataBuilder inactive() {
        this.active = false;
        return this;
    }

    public CategoryJpaEntity build() {
        return new CategoryJpaEntity(id, name, active);
    }
}
