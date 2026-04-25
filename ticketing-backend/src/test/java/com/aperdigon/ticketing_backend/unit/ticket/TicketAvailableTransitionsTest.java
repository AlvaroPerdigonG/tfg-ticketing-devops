package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketAvailableTransitionsTest {

    @Test
    void exposes_available_transitions_from_domain_ticket_state() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC);
        var ticket = DomainTestDataFactory.openTicket("Title", "Description", TicketPriority.MEDIUM);

        assertEquals(List.of(TicketStatus.IN_PROGRESS), ticket.availableTransitions());

        ticket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        assertEquals(List.of(TicketStatus.ON_HOLD, TicketStatus.RESOLVED), ticket.availableTransitions());

        ticket.changeStatus(TicketStatus.ON_HOLD, clock);
        assertEquals(List.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED), ticket.availableTransitions());

        ticket.changeStatus(TicketStatus.RESOLVED, clock);
        assertEquals(List.of(), ticket.availableTransitions());
    }
}
