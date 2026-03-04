package com.aperdigon.ticketing_backend.application.tickets.list;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ListMyTicketsUseCase {

    private final TicketRepository ticketRepository;

    public ListMyTicketsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
    }

    public Page<Ticket> execute(ListMyTicketsQuery query) {
        Guard.notNull(query, "query");
        return ticketRepository.findMyTickets(
                query.actor().id(),
                query.status(),
                query.q(),
                query.pageable()
        );
    }
}
