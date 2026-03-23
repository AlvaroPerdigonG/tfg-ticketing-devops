package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.assign.AssignTicketToMeCommand;
import com.aperdigon.ticketing_backend.application.tickets.assign.AssignTicketToMeUseCase;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketAlreadyAssigned;
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

public final class AssignTicketToMeUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-AGENT-04", level = TestLevel.UNIT, feature = "tickets-agent.feature")
    void agent_can_assign_ticket_to_self_and_event_is_recorded() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticketRepository.save(ticket);

        UserId agentId = UserId.of(UUID.randomUUID());

        var result = useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(agentId, UserRole.AGENT)));
        var events = eventRepository.findByTicketId(ticket.id());

        assertEquals(ticket.id(), result.ticketId());
        assertEquals(agentId, result.assignedToUserId());
        assertEquals(agentId, ticketRepository.findById(ticket.id()).orElseThrow().assignedTo());
        assertEquals(1, events.size());
        assertEquals(TicketEventType.ASSIGNED_TO_ME, events.get(0).type());
        assertEquals(Map.of("assignedToUserId", agentId.value().toString()), events.get(0).payload());
        assertEquals(Instant.parse("2026-02-14T10:00:00Z"), events.get(0).createdAt());
    }

    @Test
    void admin_can_assign_ticket_to_self() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticketRepository.save(ticket);

        UserId adminId = UserId.of(UUID.randomUUID());

        var result = useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(adminId, UserRole.ADMIN)));

        assertEquals(adminId, result.assignedToUserId());
        assertEquals(adminId, ticketRepository.findById(ticket.id()).orElseThrow().assignedTo());
    }

    @Test
    void user_cannot_assign_ticket() {
        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepository, eventRepository, Clock.systemUTC());

        assertThrows(ForbiddenException.class, () -> useCase.execute(new AssignTicketToMeCommand(
                com.aperdigon.ticketing_backend.domain.ticket.TicketId.of(UUID.randomUUID()),
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.USER)
        )));
        assertEquals(0, eventRepository.allEvents().size());
    }

    @Test
    void throws_not_found_if_ticket_does_not_exist() {
        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepository, eventRepository, Clock.systemUTC());

        assertThrows(NotFoundException.class, () -> useCase.execute(new AssignTicketToMeCommand(
                com.aperdigon.ticketing_backend.domain.ticket.TicketId.of(UUID.randomUUID()),
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN)
        )));
        assertEquals(0, eventRepository.allEvents().size());
    }

    @Test
    void rejects_assignment_when_ticket_is_already_assigned() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepository, eventRepository, clock);

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("T", "D", category, creator, clock);
        ticketRepository.save(ticket);

        UserId firstAgent = UserId.of(UUID.randomUUID());
        useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(firstAgent, UserRole.AGENT)));

        UserId secondAgent = UserId.of(UUID.randomUUID());
        assertThrows(TicketAlreadyAssigned.class, () ->
                useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(secondAgent, UserRole.AGENT)))
        );
        assertEquals(1, eventRepository.findByTicketId(ticket.id()).size());
    }
}
