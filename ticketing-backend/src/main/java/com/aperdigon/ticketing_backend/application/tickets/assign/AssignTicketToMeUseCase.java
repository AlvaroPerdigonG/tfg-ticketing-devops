package com.aperdigon.ticketing_backend.application.tickets.assign;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketAlreadyAssigned;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AssignTicketToMeUseCase {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final Clock clock;

    public AssignTicketToMeUseCase(TicketRepository ticketRepository, TicketEventRepository ticketEventRepository, Clock clock) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
        this.ticketEventRepository = Guard.notNull(ticketEventRepository, "ticketEventRepository");
        this.clock = Guard.notNull(clock, "clock");
    }

    public AssignTicketToMeResult execute(AssignTicketToMeCommand command) {
        Guard.notNull(command, "command");

        if (!command.actor().isAgentOrAdmin()) {
            throw new ForbiddenException("Only AGENT/ADMIN can assign tickets");
        }

        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + command.ticketId().value()));

        if (ticket.assignedTo() != null) {
            throw new TicketAlreadyAssigned();
        }

        ticket.assignTo(command.actor().id(), clock);
        ticketRepository.save(ticket);
        ticketEventRepository.save(new TicketEvent(
                UUID.randomUUID(),
                ticket.id(),
                TicketEventType.ASSIGNED_TO_ME,
                command.actor().id(),
                Map.of("assignedToUserId", command.actor().id().value().toString()),
                Instant.now(clock)
        ));

        return new AssignTicketToMeResult(ticket.id(), command.actor().id());
    }
}
