package com.aperdigon.ticketing_backend.application.tickets.change_status;

import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;

public record ChangeTicketStatusResult(TicketId ticketId, TicketStatus status) {}
