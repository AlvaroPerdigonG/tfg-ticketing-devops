package com.aperdigon.ticketing_backend.application.tickets.delete;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;

public record DeleteTicketCommand(TicketId ticketId, CurrentUser actor) {
}
