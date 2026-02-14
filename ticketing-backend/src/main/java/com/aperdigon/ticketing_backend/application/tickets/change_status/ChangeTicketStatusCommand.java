package com.aperdigon.ticketing_backend.application.tickets.change_status;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

public record ChangeTicketStatusCommand(
        TicketId ticketId,
        TicketStatus newStatus,
        CurrentUser actor
) {
    public ChangeTicketStatusCommand {
        Guard.notNull(ticketId, "ticketId");
        Guard.notNull(newStatus, "newStatus");
        Guard.notNull(actor, "actor");
    }
}
