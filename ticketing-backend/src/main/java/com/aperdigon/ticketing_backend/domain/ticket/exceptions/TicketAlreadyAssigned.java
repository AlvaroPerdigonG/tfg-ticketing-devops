package com.aperdigon.ticketing_backend.domain.ticket.exceptions;

import com.aperdigon.ticketing_backend.domain.shared.exception.DomainException;

public final class TicketAlreadyAssigned extends DomainException {
    public TicketAlreadyAssigned() {
        super("Ticket is already assigned");
    }
}

