package com.aperdigon.ticketing_backend.test_support;

import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTicketEventRepository implements TicketEventRepository {
    private final List<TicketEvent> store = new ArrayList<>();

    @Override
    public TicketEvent save(TicketEvent event) {
        store.add(event);
        return event;
    }

    @Override
    public List<TicketEvent> findByTicketId(TicketId ticketId) {
        return store.stream().filter(event -> event.ticketId().equals(ticketId)).toList();
    }

    public List<TicketEvent> allEvents() {
        return List.copyOf(store);
    }
}
