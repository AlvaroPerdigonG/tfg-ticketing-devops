package com.aperdigon.ticketing_backend.application.tickets.assign;

import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public record AssignTicketToMeCommand(TicketId ticketId, CurrentUser actor) {
    public AssignTicketToMeCommand {
        Guard.notNull(ticketId, "ticketId");
        Guard.notNull(actor, "actor");
    }
}
