package com.aperdigon.ticketing_backend.infrastructure.persistence.adapter;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper.CategoryMapper;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public Optional<Category> findByName(String name) {
        return springRepo.findByNameIgnoreCase(name).map(CategoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return springRepo.findByOrderByNameAsc().stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findActive() {
        return springRepo.findByIsActiveTrueOrderByNameAsc().stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public Category save(Category category) {
        var savedEntity = springRepo.save(CategoryMapper.toJpa(category));
        return CategoryMapper.toDomain(savedEntity);
    }
}
