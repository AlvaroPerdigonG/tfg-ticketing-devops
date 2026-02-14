package com.aperdigon.ticketing_backend.application.tickets.change_status;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;

import java.time.Clock;

public final class ChangeTicketStatusUseCase {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    public ChangeTicketStatusUseCase(TicketRepository ticketRepository, Clock clock) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
        this.clock = Guard.notNull(clock, "clock");
    }

    public ChangeTicketStatusResult execute(ChangeTicketStatusCommand command) {
        Guard.notNull(command, "command");

        if (!command.actor().isAgentOrAdmin()) {
            throw new ForbiddenException("Only AGENT/ADMIN can change ticket status");
        }

        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + command.ticketId().value()));

        ticket.changeStatus(command.newStatus(), clock);
        ticketRepository.save(ticket);

        return new ChangeTicketStatusResult(ticket.id(), ticket.status());
    }
}
