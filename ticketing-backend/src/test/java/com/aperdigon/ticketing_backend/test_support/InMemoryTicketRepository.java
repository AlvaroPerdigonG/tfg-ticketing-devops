package com.aperdigon.ticketing_backend.test_support;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryTicketRepository implements TicketRepository {
    private final Map<TicketId, Ticket> store = new HashMap<>();

    @Override
    public Ticket save(Ticket ticket) {
        store.put(ticket.id(), ticket);
        return ticket;
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Page<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, Pageable pageable) {
        return new PageImpl<>(store.values().stream().toList(), pageable, store.size());
    }

    @Override
    public Page<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, Pageable pageable) {
        return new PageImpl<>(store.values().stream().toList(), pageable, store.size());
    }
}
