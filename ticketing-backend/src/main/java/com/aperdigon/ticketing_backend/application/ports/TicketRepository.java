package com.aperdigon.ticketing_backend.application.ports;

import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(TicketId id);

    Page<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, Pageable pageable);

    Page<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, Pageable pageable);
}
