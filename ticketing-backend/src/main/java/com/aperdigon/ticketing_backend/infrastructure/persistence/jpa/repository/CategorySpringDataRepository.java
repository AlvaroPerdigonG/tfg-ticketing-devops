package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategorySpringDataRepository extends JpaRepository<CategoryJpaEntity, UUID> {
    Optional<CategoryJpaEntity> findByNameIgnoreCase(String name);
    List<CategoryJpaEntity> findByOrderByNameAsc();
    List<CategoryJpaEntity> findByIsActiveTrueOrderByNameAsc();
}
