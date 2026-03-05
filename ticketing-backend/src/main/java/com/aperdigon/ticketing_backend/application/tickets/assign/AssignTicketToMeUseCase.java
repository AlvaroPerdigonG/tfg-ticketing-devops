package com.aperdigon.ticketing_backend.application.tickets.assign;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class AssignTicketToMeUseCase {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    public AssignTicketToMeUseCase(TicketRepository ticketRepository, Clock clock) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
        this.clock = Guard.notNull(clock, "clock");
    }

    public AssignTicketToMeResult execute(AssignTicketToMeCommand command) {
        Guard.notNull(command, "command");

        if (!command.actor().isAgentOrAdmin()) {
            throw new ForbiddenException("Only AGENT/ADMIN can assign tickets");
        }

        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + command.ticketId().value()));

        ticket.assignTo(command.actor().id(), clock);
        ticketRepository.save(ticket);

        return new AssignTicketToMeResult(ticket.id(), command.actor().id());
    }
}
