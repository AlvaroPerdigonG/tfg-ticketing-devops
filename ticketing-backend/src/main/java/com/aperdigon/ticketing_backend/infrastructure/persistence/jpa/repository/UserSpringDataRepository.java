package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);
}
