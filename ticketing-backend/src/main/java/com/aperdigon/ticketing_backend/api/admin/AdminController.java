package com.aperdigon.ticketing_backend.api.admin;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import jakarta.validation.Valid;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AdminController(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/categories")
    public List<AdminCategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(AdminCategoryResponse::from)
                .toList();
    }

    @PostMapping("/categories")
    public ResponseEntity<AdminCategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        var trimmedName = request.name().trim();
        if (categoryRepository.findByName(trimmedName).isPresent()) {
            throw new InvalidArgumentException("Category already exists");
        }

        var category = new Category(new CategoryId(UUID.randomUUID()), trimmedName, true);
        var saved = categoryRepository.save(category);

        return ResponseEntity.created(URI.create("/api/admin/categories/" + saved.id().value()))
                .body(AdminCategoryResponse.from(saved));
    }

    @PatchMapping("/categories/{categoryId}")
    public AdminCategoryResponse updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        var existing = categoryRepository.findById(new CategoryId(categoryId))
                .orElseThrow(() -> new NotFoundException("Category not found"));

        var updated = new Category(existing.id(), request.name().trim(), request.isActive());
        var saved = categoryRepository.save(updated);
        return AdminCategoryResponse.from(saved);
    }

    @GetMapping("/users")
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @PatchMapping("/users/{userId}/active")
    public AdminUserResponse updateUserActive(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserActiveRequest request
    ) {
        var existing = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        var updated = new User(
                existing.id(),
                existing.email(),
                existing.displayName(),
                existing.passwordHash(),
                existing.role(),
                request.isActive()
        );

        var saved = userRepository.save(updated);
        return AdminUserResponse.from(saved);
    }
}
