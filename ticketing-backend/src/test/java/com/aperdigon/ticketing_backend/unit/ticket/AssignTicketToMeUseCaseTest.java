package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.assign.AssignTicketToMeCommand;
import com.aperdigon.ticketing_backend.application.tickets.assign.AssignTicketToMeUseCase;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketAlreadyAssigned;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssignTicketToMeUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-AGENT-04", level = TestLevel.UNIT, feature = "tickets-agent.feature")
    void agent_can_assign_ticket_to_self() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepo = new InMemoryTicketRepository();
        var eventRepo = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepo, eventRepo, clock);

        User creator = new User(UserId.of(UUID.randomUUID()), "u@test.com", "U", "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG", UserRole.USER, true);
        Category category = new Category(CategoryId.of(UUID.randomUUID()), "General", true);
        Ticket ticket = Ticket.openNew("T", "D", category, creator, clock);
        ticketRepo.save(ticket);

        UserId agentId = UserId.of(UUID.randomUUID());

        useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(agentId, UserRole.AGENT)));

        assertEquals(agentId, ticketRepo.findById(ticket.id()).orElseThrow().assignedTo());
    }

    @Test
    void user_cannot_assign_ticket() {
        var ticketRepo = new InMemoryTicketRepository();
        var eventRepo = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepo, eventRepo, Clock.systemUTC());

        assertThrows(ForbiddenException.class, () -> useCase.execute(new AssignTicketToMeCommand(
                TicketId.of(UUID.randomUUID()),
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.USER)
        )));
    }

    @Test
    void throws_not_found_if_ticket_does_not_exist() {
        var ticketRepo = new InMemoryTicketRepository();
        var eventRepo = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepo, eventRepo, Clock.systemUTC());

        assertThrows(NotFoundException.class, () -> useCase.execute(new AssignTicketToMeCommand(
                TicketId.of(UUID.randomUUID()),
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN)
        )));
    }

    @Test
    void cannot_assign_ticket_twice() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepo = new InMemoryTicketRepository();
        var eventRepo = new InMemoryTicketEventRepository();
        var useCase = new AssignTicketToMeUseCase(ticketRepo, eventRepo, clock);

        User creator = new User(UserId.of(UUID.randomUUID()), "u@test.com", "U", "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG", UserRole.USER, true);
        Category category = new Category(CategoryId.of(UUID.randomUUID()), "General", true);
        Ticket ticket = Ticket.openNew("T", "D", category, creator, clock);
        ticketRepo.save(ticket);

        UserId firstAgent = UserId.of(UUID.randomUUID());
        useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(firstAgent, UserRole.AGENT)));

        UserId secondAgent = UserId.of(UUID.randomUUID());
        assertThrows(TicketAlreadyAssigned.class, () ->
                useCase.execute(new AssignTicketToMeCommand(ticket.id(), new CurrentUser(secondAgent, UserRole.AGENT)))
        );
    }
}
