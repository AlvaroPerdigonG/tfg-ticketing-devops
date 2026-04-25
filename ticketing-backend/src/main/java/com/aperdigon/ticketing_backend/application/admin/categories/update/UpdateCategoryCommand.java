package com.aperdigon.ticketing_backend.application.admin.categories.update;

import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public record UpdateCategoryCommand(
        CategoryId categoryId,
        String name,
        boolean isActive
) {
    public UpdateCategoryCommand {
        Guard.notNull(categoryId, "categoryId");
        Guard.notBlank(name, "name");
    }
}
