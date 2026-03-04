package com.aperdigon.ticketing_backend.application.tickets.get;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import org.springframework.stereotype.Service;

@Service
public class GetTicketUseCase {

    private final TicketRepository ticketRepository;

    public GetTicketUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
    }

    public Ticket execute(TicketId ticketId, CurrentUser actor) {
        Guard.notNull(ticketId, "ticketId");
        Guard.notNull(actor, "actor");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId.value()));

        if (!actor.isAgentOrAdmin() && !ticket.createdBy().equals(actor.id())) {
            throw new ForbiddenException("You cannot access this ticket");
        }

        return ticket;
    }
}
