package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper;

import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;

public final class CategoryMapper {
    private CategoryMapper() {}

    public static Category toDomain(CategoryJpaEntity e) {
        return new Category(
                new CategoryId(e.getId()),
                e.getName(),
                e.isActive()
        );
    }
}
