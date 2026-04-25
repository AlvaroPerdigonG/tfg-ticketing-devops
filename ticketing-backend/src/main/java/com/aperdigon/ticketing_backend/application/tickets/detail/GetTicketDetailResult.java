package com.aperdigon.ticketing_backend.application.tickets.detail;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GetTicketDetailResult(
        Ticket ticket,
        List<TicketEvent> events,
        Map<UUID, String> userNamesById
) {
    public GetTicketDetailResult {
        Guard.notNull(ticket, "ticket");
        events = List.copyOf(Guard.notNull(events, "events"));
        userNamesById = Map.copyOf(Guard.notNull(userNamesById, "userNamesById"));
    }

    public String resolveUserDisplayName(UUID userId) {
        return userNamesById.getOrDefault(userId, userId.toString());
    }
}
