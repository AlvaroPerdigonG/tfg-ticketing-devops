package com.aperdigon.ticketing_backend.application.tickets.list;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import org.springframework.data.domain.Pageable;

public record ListTicketsQuery(
        CurrentUser actor,
        TicketQueueScope scope,
        TicketStatus status,
        String q,
        Pageable pageable
) {}
