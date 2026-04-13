package com.aperdigon.ticketing_backend.api.categories;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category lookup endpoints")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    @Operation(summary = "List active categories")
    public List<CategoryResponse> listActive() {
        return categoryRepository.findActive().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
