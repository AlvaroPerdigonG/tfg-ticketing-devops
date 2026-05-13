package com.aperdigon.ticketing_backend.unit.admin;

import com.aperdigon.ticketing_backend.application.admin.categories.create.CreateCategoryCommand;
import com.aperdigon.ticketing_backend.application.admin.categories.create.CreateCategoryUseCase;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryCategoryRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CreateCategoryUseCaseTest {

    @Test
    void creates_active_category_with_trimmed_name() {
        var repository = new InMemoryCategoryRepository();
        var useCase = new CreateCategoryUseCase(repository);

        var category = useCase.execute(new CreateCategoryCommand("  Hardware  "));

        assertEquals("Hardware", category.name());
        assertTrue(category.isActive());
        assertEquals(category, repository.findById(category.id()).orElseThrow());
    }

    @Test
    void rejects_duplicate_category_name_case_insensitively() {
        var repository = new InMemoryCategoryRepository();
        repository.put(DomainTestDataFactory.activeCategory("Hardware"));
        var useCase = new CreateCategoryUseCase(repository);

        assertThrows(InvalidArgumentException.class, () -> useCase.execute(new CreateCategoryCommand(" hardware ")));
    }
}
