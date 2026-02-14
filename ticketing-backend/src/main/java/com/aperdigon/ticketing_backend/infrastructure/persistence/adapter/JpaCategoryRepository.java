package com.aperdigon.ticketing_backend.infrastructure.persistence.adapter;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper.CategoryMapper;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCategoryRepository implements CategoryRepository {

    private final CategorySpringDataRepository springRepo;

    public JpaCategoryRepository(CategorySpringDataRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return springRepo.findById(id.value()).map(CategoryMapper::toDomain);
    }
}
