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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
        List<Ticket> filtered = sortedTickets()
                .filter(ticket -> ticket.createdBy().equals(createdBy))
                .filter(ticket -> status == null || ticket.status() == status)
                .filter(ticket -> matchesQuery(ticket, q))
                .toList();

        return toPage(filtered, pageable);
    }

    @Override
    public Page<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, Pageable pageable) {
        List<Ticket> filtered = sortedTickets()
                .filter(ticket -> matchesScope(ticket, actorId, scope))
                .filter(ticket -> status == null || ticket.status() == status)
                .filter(ticket -> matchesQuery(ticket, q))
                .toList();

        return toPage(filtered, pageable);
    }

    private Stream<Ticket> sortedTickets() {
        return store.values().stream()
                .sorted(Comparator.comparing(Ticket::createdAt).thenComparing(ticket -> ticket.id().value()));
    }

    private boolean matchesScope(Ticket ticket, UserId actorId, TicketQueueScope scope) {
        TicketQueueScope effectiveScope = scope == null ? TicketQueueScope.ALL : scope;
        return switch (effectiveScope) {
            case UNASSIGNED -> ticket.assignedTo() == null;
            case MINE -> actorId.equals(ticket.assignedTo());
            case OTHERS -> ticket.assignedTo() != null && !actorId.equals(ticket.assignedTo());
            case ALL -> true;
        };
    }

    private boolean matchesQuery(Ticket ticket, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }

        String normalizedQuery = q.trim().toLowerCase();
        return ticket.title().toLowerCase().contains(normalizedQuery)
                || ticket.description().toLowerCase().contains(normalizedQuery);
    }

    private Page<Ticket> toPage(List<Ticket> filtered, Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(filtered, Pageable.unpaged(), filtered.size());
        }

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }

        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
}
