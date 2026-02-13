package com.aperdigon.ticketing_backend.domain.category;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

import java.util.UUID;


public record CategoryId(UUID value) {
    public CategoryId {
        Guard.notNull(value, "categoryId");
    }

    public static CategoryId of(UUID value) { return new CategoryId(value); }
}
