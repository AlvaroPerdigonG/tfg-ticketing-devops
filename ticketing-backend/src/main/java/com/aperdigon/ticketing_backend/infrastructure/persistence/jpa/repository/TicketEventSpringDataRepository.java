package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketEventSpringDataRepository extends JpaRepository<TicketEventJpaEntity, UUID> {
    List<TicketEventJpaEntity> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
}
