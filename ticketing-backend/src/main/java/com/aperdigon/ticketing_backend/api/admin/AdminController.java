package com.aperdigon.ticketing_backend.api.admin;

import com.aperdigon.ticketing_backend.application.admin.categories.create.CreateCategoryCommand;
import com.aperdigon.ticketing_backend.application.admin.categories.create.CreateCategoryUseCase;
import com.aperdigon.ticketing_backend.application.admin.categories.list.ListAdminCategoriesUseCase;
import com.aperdigon.ticketing_backend.application.admin.categories.update.UpdateCategoryCommand;
import com.aperdigon.ticketing_backend.application.admin.categories.update.UpdateCategoryUseCase;
import com.aperdigon.ticketing_backend.application.admin.users.list.ListAdminUsersUseCase;
import com.aperdigon.ticketing_backend.application.admin.users.update_active.UpdateUserActiveCommand;
import com.aperdigon.ticketing_backend.application.admin.users.update_active.UpdateUserActiveUseCase;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {

    private final ListAdminCategoriesUseCase listAdminCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final ListAdminUsersUseCase listAdminUsersUseCase;
    private final UpdateUserActiveUseCase updateUserActiveUseCase;

    public AdminController(
            ListAdminCategoriesUseCase listAdminCategoriesUseCase,
            CreateCategoryUseCase createCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            ListAdminUsersUseCase listAdminUsersUseCase,
            UpdateUserActiveUseCase updateUserActiveUseCase
    ) {
        this.listAdminCategoriesUseCase = listAdminCategoriesUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.listAdminUsersUseCase = listAdminUsersUseCase;
        this.updateUserActiveUseCase = updateUserActiveUseCase;
    }

    @GetMapping("/categories")
    @Operation(summary = "List all categories (admin)")
    public List<AdminCategoryResponse> listCategories() {
        return listAdminCategoriesUseCase.execute().stream()
                .map(AdminCategoryResponse::from)
                .toList();
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category (admin)")
    public ResponseEntity<AdminCategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        var saved = createCategoryUseCase.execute(new CreateCategoryCommand(request.name()));

        return ResponseEntity.created(URI.create("/api/admin/categories/" + saved.id().value()))
                .body(AdminCategoryResponse.from(saved));
    }

    @PatchMapping("/categories/{categoryId}")
    @Operation(summary = "Update category (admin)")
    public AdminCategoryResponse updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        var saved = updateCategoryUseCase.execute(new UpdateCategoryCommand(
                new CategoryId(categoryId),
                request.name(),
                request.isActive()
        ));
        return AdminCategoryResponse.from(saved);
    }

    @GetMapping("/users")
    @Operation(summary = "List all users (admin)")
    public List<AdminUserResponse> listUsers() {
        return listAdminUsersUseCase.execute().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @PatchMapping("/users/{userId}/active")
    @Operation(summary = "Activate or deactivate user (admin)")
    public AdminUserResponse updateUserActive(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserActiveRequest request
    ) {
        var saved = updateUserActiveUseCase.execute(new UpdateUserActiveCommand(
                new UserId(userId),
                request.isActive()
        ));
        return AdminUserResponse.from(saved);
    }
}
