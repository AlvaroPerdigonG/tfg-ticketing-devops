package com.aperdigon.ticketing_backend.api.tickets.list;

import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketSummaryResponse(
        UUID id,
        String title,
        TicketStatus status,
        TicketPriority priority,
        Instant createdAt,
        Instant updatedAt,
        UUID createdByUserId,
        UUID assignedToUserId
) {
    public static TicketSummaryResponse from(Ticket ticket) {
        return new TicketSummaryResponse(
                ticket.id().value(),
                ticket.title(),
                ticket.status(),
                ticket.priority(),
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.createdBy().value(),
                ticket.assignedTo() == null ? null : ticket.assignedTo().value()
        );
    }
}
