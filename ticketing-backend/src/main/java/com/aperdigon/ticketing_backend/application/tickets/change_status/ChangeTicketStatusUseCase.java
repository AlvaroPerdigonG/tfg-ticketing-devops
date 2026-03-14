package com.aperdigon.ticketing_backend.application.tickets.change_status;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public final class ChangeTicketStatusUseCase {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final Clock clock;

    public ChangeTicketStatusUseCase(TicketRepository ticketRepository, TicketEventRepository ticketEventRepository, Clock clock) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
        this.ticketEventRepository = Guard.notNull(ticketEventRepository, "ticketEventRepository");
        this.clock = Guard.notNull(clock, "clock");
    }

    public ChangeTicketStatusResult execute(ChangeTicketStatusCommand command) {
        Guard.notNull(command, "command");

        if (!command.actor().isAgentOrAdmin()) {
            throw new ForbiddenException("Only AGENT/ADMIN can change ticket status");
        }

        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + command.ticketId().value()));

        var previousStatus = ticket.status();
        ticket.changeStatus(command.newStatus(), clock);
        ticketRepository.save(ticket);
        ticketEventRepository.save(new TicketEvent(
                UUID.randomUUID(),
                ticket.id(),
                TicketEventType.STATUS_CHANGED,
                command.actor().id(),
                Map.of("from", previousStatus.name(), "to", ticket.status().name()),
                Instant.now(clock)
        ));

        return new ChangeTicketStatusResult(ticket.id(), ticket.status());
    }
}
