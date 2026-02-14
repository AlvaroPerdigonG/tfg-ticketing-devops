package com.aperdigon.ticketing_backend.test_support;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;

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
}
