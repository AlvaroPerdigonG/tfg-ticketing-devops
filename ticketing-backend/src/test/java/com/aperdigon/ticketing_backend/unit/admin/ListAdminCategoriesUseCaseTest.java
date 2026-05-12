package com.aperdigon.ticketing_backend.unit.admin;

import com.aperdigon.ticketing_backend.application.admin.categories.list.ListAdminCategoriesUseCase;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryCategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ListAdminCategoriesUseCaseTest {

    @Test
    void lists_active_and_inactive_categories_sorted_by_name() {
        var repository = new InMemoryCategoryRepository();
        var zeta = DomainTestDataFactory.activeCategory("Zeta");
        var alphaInactive = new Category(CategoryId.of(UUID.randomUUID()), "Alpha", false);
        repository.put(zeta);
        repository.put(alphaInactive);
        var useCase = new ListAdminCategoriesUseCase(repository);

        var result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals(alphaInactive.id(), result.get(0).id());
        assertEquals(zeta.id(), result.get(1).id());
    }
}
