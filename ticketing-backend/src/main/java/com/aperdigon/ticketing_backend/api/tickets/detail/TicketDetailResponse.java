package com.aperdigon.ticketing_backend.api.tickets.detail;

import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketDetailResponse(
        UUID id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Instant createdAt,
        Instant updatedAt,
        UUID createdByUserId,
        UUID assignedToUserId,
        UUID categoryId
) {
    public static TicketDetailResponse from(Ticket ticket) {
        return new TicketDetailResponse(
                ticket.id().value(),
                ticket.title(),
                ticket.description(),
                ticket.status(),
                ticket.priority(),
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.createdBy().value(),
                ticket.assignedTo() == null ? null : ticket.assignedTo().value(),
                ticket.categoryId().value()
        );
    }
}
