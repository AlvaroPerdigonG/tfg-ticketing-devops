package com.aperdigon.ticketing_backend.domain.ticket;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

import java.util.UUID;


public record TicketId(UUID value) {
    public TicketId {
        Guard.notNull(value, "ticketId");
    }

    public static TicketId of(UUID value) {
        return new TicketId(value);
    }
}
