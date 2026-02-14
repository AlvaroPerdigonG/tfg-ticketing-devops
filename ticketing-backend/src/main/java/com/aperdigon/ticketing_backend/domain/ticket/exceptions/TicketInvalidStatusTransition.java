package com.aperdigon.ticketing_backend.domain.ticket.exceptions;

import com.aperdigon.ticketing_backend.domain.shared.exception.DomainException;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

public final class TicketInvalidStatusTransition extends DomainException {
    public TicketInvalidStatusTransition(TicketStatus from, TicketStatus to) {
        super("Invalid status transition: " + from + " -> " + to);
    }
}
