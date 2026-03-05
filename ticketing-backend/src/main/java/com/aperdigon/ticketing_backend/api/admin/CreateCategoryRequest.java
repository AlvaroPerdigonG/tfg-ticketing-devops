package com.aperdigon.ticketing_backend.api.admin;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String name
) {}
