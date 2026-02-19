package com.aperdigon.ticketing_backend.api.tickets.change_status;

import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTicketStatusRequest(@NotNull TicketStatus status) {}
