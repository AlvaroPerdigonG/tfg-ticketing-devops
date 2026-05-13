package com.aperdigon.ticketing_backend.unit.admin;

import com.aperdigon.ticketing_backend.application.admin.categories.update.UpdateCategoryCommand;
import com.aperdigon.ticketing_backend.application.admin.categories.update.UpdateCategoryUseCase;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryCategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UpdateCategoryUseCaseTest {

    @Test
    void updates_category_name_and_active_flag() {
        var repository = new InMemoryCategoryRepository();
        var existing = DomainTestDataFactory.activeCategory("Hardware");
        repository.put(existing);
        var useCase = new UpdateCategoryUseCase(repository);

        var updated = useCase.execute(new UpdateCategoryCommand(existing.id(), "  Devices  ", false));

        assertEquals(existing.id(), updated.id());
        assertEquals("Devices", updated.name());
        assertFalse(updated.isActive());
        assertEquals(updated, repository.findById(existing.id()).orElseThrow());
    }

    @Test
    void rejects_missing_category() {
        var useCase = new UpdateCategoryUseCase(new InMemoryCategoryRepository());

        assertThrows(NotFoundException.class, () -> useCase.execute(new UpdateCategoryCommand(
                CategoryId.of(UUID.randomUUID()),
                "Hardware",
                true
        )));
    }
}
