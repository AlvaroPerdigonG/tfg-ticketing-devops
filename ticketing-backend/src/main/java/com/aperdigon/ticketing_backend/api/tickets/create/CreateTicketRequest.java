package com.aperdigon.ticketing_backend.api.tickets.create;

import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull UUID categoryId,
        @NotNull TicketPriority priority
) {}
