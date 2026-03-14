package com.aperdigon.ticketing_backend.application.ports;

import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;

import java.util.List;

public interface TicketEventRepository {
    TicketEvent save(TicketEvent event);
    List<TicketEvent> findByTicketId(TicketId ticketId);
}
