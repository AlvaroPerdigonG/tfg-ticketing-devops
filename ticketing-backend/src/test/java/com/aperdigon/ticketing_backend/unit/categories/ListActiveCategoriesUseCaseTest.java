package com.aperdigon.ticketing_backend.unit.categories;

import com.aperdigon.ticketing_backend.application.categories.list.ListActiveCategoriesUseCase;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryCategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ListActiveCategoriesUseCaseTest {

    @Test
    void lists_only_active_categories_sorted_by_name() {
        var repository = new InMemoryCategoryRepository();
        var zeta = DomainTestDataFactory.activeCategory("Zeta");
        var alpha = DomainTestDataFactory.activeCategory("Alpha");
        repository.put(zeta);
        repository.put(new Category(CategoryId.of(UUID.randomUUID()), "Inactive", false));
        repository.put(alpha);
        var useCase = new ListActiveCategoriesUseCase(repository);

        var result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals(alpha.id(), result.get(0).id());
        assertEquals(zeta.id(), result.get(1).id());
    }
}
