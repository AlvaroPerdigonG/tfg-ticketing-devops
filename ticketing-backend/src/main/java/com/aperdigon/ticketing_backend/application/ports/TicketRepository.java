package com.aperdigon.ticketing_backend.application.ports;

import com.aperdigon.ticketing_backend.application.shared.pagination.PageQuery;
import com.aperdigon.ticketing_backend.application.shared.pagination.PagedResult;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.AgentTicketCount;
import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(TicketId id);

    PagedResult<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, PageQuery pageQuery);

    PagedResult<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, PageQuery pageQuery);

    long countUnassigned();

    long countAssignedTo(UserId assigneeId);

    long countByStatus(TicketStatus status);

    List<AgentTicketCount> countAssignedByAssigneeRoles(Set<UserRole> roles);

    List<AgentTicketCount> countByStatusGroupedByAssigneeRoles(TicketStatus status, Set<UserRole> roles);
}
