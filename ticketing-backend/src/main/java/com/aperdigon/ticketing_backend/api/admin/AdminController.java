package com.aperdigon.ticketing_backend.api.admin;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AdminController(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/categories")
    @Operation(summary = "List all categories (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories loaded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<AdminCategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(AdminCategoryResponse::from)
                .toList();
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created",
                    content = @Content(schema = @Schema(implementation = AdminCategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
    @Operation(summary = "Update category (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated",
                    content = @Content(schema = @Schema(implementation = AdminCategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
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
    @Operation(summary = "List all users (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users loaded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @PatchMapping("/users/{userId}/active")
    @Operation(summary = "Activate or deactivate user (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated",
                    content = @Content(schema = @Schema(implementation = AdminUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
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
