package com.aperdigon.ticketing_backend.domain.ticket_event;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.user.UserId;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TicketEvent(
        UUID id,
        TicketId ticketId,
        TicketEventType type,
        UserId actorUserId,
        Map<String, String> payload,
        Instant createdAt
) {
    public TicketEvent {
        Guard.notNull(id, "id");
        Guard.notNull(ticketId, "ticketId");
        Guard.notNull(type, "type");
        Guard.notNull(payload, "payload");
        Guard.notNull(createdAt, "createdAt");
    }
}
