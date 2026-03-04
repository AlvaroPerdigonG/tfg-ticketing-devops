package com.aperdigon.ticketing_backend.application.tickets.list;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ListTicketsUseCase {

    private final TicketRepository ticketRepository;

    public ListTicketsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
    }

    public Page<Ticket> execute(ListTicketsQuery query) {
        Guard.notNull(query, "query");

        if (!query.actor().isAgentOrAdmin()) {
            throw new ForbiddenException("Only AGENT/ADMIN can list operational queues");
        }

        return ticketRepository.findAgentTickets(
                query.actor().id(),
                query.scope(),
                query.status(),
                query.q(),
                query.pageable()
        );
    }
}
