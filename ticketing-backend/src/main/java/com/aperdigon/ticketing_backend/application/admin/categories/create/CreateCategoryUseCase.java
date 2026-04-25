package com.aperdigon.ticketing_backend.application.admin.categories.create;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = Guard.notNull(categoryRepository, "categoryRepository");
    }

    public Category execute(CreateCategoryCommand command) {
        Guard.notNull(command, "command");

        var trimmedName = command.name().trim();
        if (categoryRepository.findByName(trimmedName).isPresent()) {
            throw new InvalidArgumentException("Category already exists");
        }

        var category = new Category(new CategoryId(UUID.randomUUID()), trimmedName, true);
        return categoryRepository.save(category);
    }
}
