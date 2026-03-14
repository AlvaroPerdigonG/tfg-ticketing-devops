package com.aperdigon.ticketing_backend.application.tickets.add_comment;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;

public record AddTicketCommentCommand(
        TicketId ticketId,
        String content,
        CurrentUser actor
) {
}
