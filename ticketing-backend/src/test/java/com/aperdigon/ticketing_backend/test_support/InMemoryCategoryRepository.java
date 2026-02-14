package com.aperdigon.ticketing_backend.test_support;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCategoryRepository implements CategoryRepository {
    private final Map<CategoryId, Category> store = new HashMap<>();

    public void put(Category category) {
        store.put(category.id(), category);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return Optional.ofNullable(store.get(id));
    }
}
