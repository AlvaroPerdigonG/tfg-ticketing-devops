package com.aperdigon.ticketing_backend.api.categories;

import com.aperdigon.ticketing_backend.domain.category.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.id().value(), category.name());
    }
}
