package com.aperdigon.ticketing_backend.application.ports;

import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.AgentTicketCount;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(TicketId id);

    Page<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, Pageable pageable);

    Page<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, Pageable pageable);

    long countUnassigned();

    long countAssignedTo(UserId assigneeId);

    long countByStatus(TicketStatus status);

    List<AgentTicketCount> countAssignedByAssigneeRoles(Set<UserRole> roles);

    List<AgentTicketCount> countByStatusGroupedByAssigneeRoles(TicketStatus status, Set<UserRole> roles);
}
