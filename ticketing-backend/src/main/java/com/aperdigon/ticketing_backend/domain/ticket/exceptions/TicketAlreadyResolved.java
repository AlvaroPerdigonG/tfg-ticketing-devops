package com.aperdigon.ticketing_backend.domain.ticket.exceptions;

import com.aperdigon.ticketing_backend.domain.shared.exception.DomainException;

public final class TicketAlreadyResolved extends DomainException {
    public TicketAlreadyResolved() {
        super("Ticket is already resolved");
    }
}
