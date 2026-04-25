package com.aperdigon.ticketing_backend.application.admin.categories.list;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAdminCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public ListAdminCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = Guard.notNull(categoryRepository, "categoryRepository");
    }

    public List<Category> execute() {
        return categoryRepository.findAll();
    }
}
