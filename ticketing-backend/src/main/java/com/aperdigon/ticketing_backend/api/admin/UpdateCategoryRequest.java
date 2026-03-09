package com.aperdigon.ticketing_backend.api.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(
        @NotBlank String name,
        @NotNull Boolean isActive
) {}
