package com.aperdigon.ticketing_backend.application.tickets.list;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.pagination.PageQuery;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

public record ListMyTicketsQuery(
        CurrentUser actor,
        TicketStatus status,
        String q,
        PageQuery pageQuery
) {}
