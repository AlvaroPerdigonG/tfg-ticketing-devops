package com.aperdigon.ticketing_backend.application.admin.categories.update;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import org.springframework.stereotype.Service;

@Service
public final class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = Guard.notNull(categoryRepository, "categoryRepository");
    }

    public Category execute(UpdateCategoryCommand command) {
        Guard.notNull(command, "command");

        var existing = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        var updated = new Category(existing.id(), command.name().trim(), command.isActive());
        return categoryRepository.save(updated);
    }
}
