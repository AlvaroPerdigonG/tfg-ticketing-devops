package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusCommand;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusResult;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusUseCase;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketInvalidStatusTransition;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketEventRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ChangeTicketStatusUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-AGENT-01", level = TestLevel.UNIT, feature = "tickets-agent.feature", note = "Valid transition from OPEN to IN_PROGRESS.")
    void agent_can_change_status_and_event_is_recorded() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticketRepository.save(ticket);

        ChangeTicketStatusResult result = useCase.execute(new ChangeTicketStatusCommand(
                ticket.id(),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        ));

        var events = eventRepository.findByTicketId(ticket.id());
        assertEquals(ticket.id(), result.ticketId());
        assertEquals(TicketStatus.IN_PROGRESS, result.status());
        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticket.id()).orElseThrow().status());
        assertEquals(1, events.size());
        assertEquals(TicketEventType.STATUS_CHANGED, events.get(0).type());
        assertEquals(Map.of("from", "OPEN", "to", "IN_PROGRESS"), events.get(0).payload());
        assertEquals(Instant.parse("2026-02-14T10:00:00Z"), events.get(0).createdAt());
    }

    @Test
    void admin_can_change_status_of_ticket() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticketRepository.save(ticket);

        var result = useCase.execute(new ChangeTicketStatusCommand(
                ticket.id(),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN)
        ));

        assertEquals(TicketStatus.IN_PROGRESS, result.status());
    }

    @Test
    @SpecificationRef(value = "TICKET-USER-05", level = TestLevel.UNIT, feature = "tickets-user.feature")
    void user_cannot_change_status() {
        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepository, eventRepository, Clock.systemUTC());

        assertThrows(ForbiddenException.class, () -> useCase.execute(new ChangeTicketStatusCommand(
                com.aperdigon.ticketing_backend.domain.ticket.TicketId.of(UUID.randomUUID()),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.USER)
        )));
        assertEquals(0, eventRepository.allEvents().size());
    }

    @Test
    void throws_not_found_if_ticket_does_not_exist() {
        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepository, eventRepository, Clock.systemUTC());

        assertThrows(NotFoundException.class, () -> useCase.execute(new ChangeTicketStatusCommand(
                com.aperdigon.ticketing_backend.domain.ticket.TicketId.of(UUID.randomUUID()),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        )));
        assertEquals(0, eventRepository.allEvents().size());
    }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-02", level = TestLevel.UNIT, feature = "tickets-agent.feature")
    void invalid_transition_is_rejected_and_no_new_event_is_recorded() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        ticketRepository.save(ticket);

        assertThrows(TicketInvalidStatusTransition.class, () -> useCase.execute(new ChangeTicketStatusCommand(
                ticket.id(),
                TicketStatus.OPEN,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        )));
        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticket.id()).orElseThrow().status());
        assertEquals(0, eventRepository.findByTicketId(ticket.id()).size());
    }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-01", level = TestLevel.UNIT, feature = "tickets-agent.feature", note = "Another valid operational transition.")
    void can_move_from_in_progress_to_on_hold() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        ticketRepository.save(ticket);

        ChangeTicketStatusResult result = useCase.execute(new ChangeTicketStatusCommand(
                ticket.id(),
                TicketStatus.ON_HOLD,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        ));

        assertEquals(TicketStatus.ON_HOLD, result.status());
    }
}
