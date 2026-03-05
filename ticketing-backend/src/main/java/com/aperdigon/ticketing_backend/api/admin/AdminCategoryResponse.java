package com.aperdigon.ticketing_backend.api.admin;

import com.aperdigon.ticketing_backend.domain.category.Category;

import java.util.UUID;

public record AdminCategoryResponse(
        UUID id,
        String name,
        boolean isActive
) {
    public static AdminCategoryResponse from(Category category) {
        return new AdminCategoryResponse(category.id().value(), category.name(), category.isActive());
    }
}
