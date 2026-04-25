package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.projection.AssigneeTicketCountProjection;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketSpringDataRepository extends JpaRepository<TicketJpaEntity, UUID>, JpaSpecificationExecutor<TicketJpaEntity> {

    @EntityGraph(attributePaths = {"comments", "createdBy", "assignedTo", "category", "comments.author"})
    Optional<TicketJpaEntity> findWithDetailsById(UUID id);

    long countByAssignedToIsNull();

    long countByAssignedTo_Id(UUID assigneeId);

    long countByStatus(TicketStatus status);

    @Query("""
            SELECT
                t.assignedTo.id AS assigneeId,
                t.assignedTo.displayName AS assigneeDisplayName,
                COUNT(t) AS ticketCount
            FROM TicketJpaEntity t
            WHERE t.assignedTo IS NOT NULL
              AND t.assignedTo.role IN :roles
            GROUP BY t.assignedTo.id, t.assignedTo.displayName
            ORDER BY COUNT(t) DESC
            """)
    List<AssigneeTicketCountProjection> countAssignedByAssigneeRoles(@Param("roles") Collection<UserRole> roles);

    @Query("""
            SELECT
                t.assignedTo.id AS assigneeId,
                t.assignedTo.displayName AS assigneeDisplayName,
                COUNT(t) AS ticketCount
            FROM TicketJpaEntity t
            WHERE t.assignedTo IS NOT NULL
              AND t.status = :status
              AND t.assignedTo.role IN :roles
            GROUP BY t.assignedTo.id, t.assignedTo.displayName
            ORDER BY COUNT(t) DESC
            """)
    List<AssigneeTicketCountProjection> countByStatusGroupedByAssigneeRoles(
            @Param("status") TicketStatus status,
            @Param("roles") Collection<UserRole> roles
    );
}
