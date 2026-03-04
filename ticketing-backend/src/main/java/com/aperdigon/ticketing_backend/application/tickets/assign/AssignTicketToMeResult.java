package com.aperdigon.ticketing_backend.application.tickets.assign;

import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.UserId;

public record AssignTicketToMeResult(TicketId ticketId, UserId assignedToUserId) {
    public AssignTicketToMeResult {
        Guard.notNull(ticketId, "ticketId");
        Guard.notNull(assignedToUserId, "assignedToUserId");
    }
}
