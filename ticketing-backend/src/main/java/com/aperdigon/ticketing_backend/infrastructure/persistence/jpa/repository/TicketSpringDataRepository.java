package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketSpringDataRepository extends JpaRepository<TicketJpaEntity, UUID> {

    // Para evitar LazyInitialization y traer comments/refs cuando se lee un ticket
    @EntityGraph(attributePaths = {"comments", "createdBy", "assignedTo", "category", "comments.author"})
    Optional<TicketJpaEntity> findWithDetailsById(UUID id);
}
