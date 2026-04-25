package com.aperdigon.ticketing_backend.application.admin.categories.create;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public record CreateCategoryCommand(String name) {
    public CreateCategoryCommand {
        Guard.notBlank(name, "name");
    }
}
