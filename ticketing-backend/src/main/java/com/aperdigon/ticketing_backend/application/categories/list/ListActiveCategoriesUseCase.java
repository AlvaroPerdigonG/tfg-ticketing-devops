package com.aperdigon.ticketing_backend.application.categories.list;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListActiveCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public ListActiveCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = Guard.notNull(categoryRepository, "categoryRepository");
    }

    public List<Category> execute() {
        return categoryRepository.findActive();
    }
}
