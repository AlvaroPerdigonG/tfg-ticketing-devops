package com.aperdigon.ticketing_backend.api.categories;

import com.aperdigon.ticketing_backend.application.categories.list.ListActiveCategoriesUseCase;
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

    private final ListActiveCategoriesUseCase listActiveCategoriesUseCase;

    public CategoryController(ListActiveCategoriesUseCase listActiveCategoriesUseCase) {
        this.listActiveCategoriesUseCase = listActiveCategoriesUseCase;
    }

    @GetMapping
    @Operation(summary = "List active categories")
    public List<CategoryResponse> listActive() {
        return listActiveCategoriesUseCase.execute().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
