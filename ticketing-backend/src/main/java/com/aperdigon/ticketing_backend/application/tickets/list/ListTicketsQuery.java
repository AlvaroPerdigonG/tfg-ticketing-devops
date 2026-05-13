package com.aperdigon.ticketing_backend.application.tickets.list;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.pagination.PageQuery;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

public record ListTicketsQuery(
        CurrentUser actor,
        TicketQueueScope scope,
        TicketStatus status,
        String q,
        PageQuery pageQuery
) {}
