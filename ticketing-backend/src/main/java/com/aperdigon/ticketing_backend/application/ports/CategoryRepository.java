package com.aperdigon.ticketing_backend.application.ports;

import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(CategoryId id);
    Optional<Category> findByName(String name);
    List<Category> findAll();
    List<Category> findActive();
    Category save(Category category);
}
