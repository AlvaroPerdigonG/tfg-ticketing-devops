package com.aperdigon.ticketing_backend.unit.ticket;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusCommand;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusResult;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusUseCase;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketInvalidStatusTransition;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import org.junit.jupiter.api.Test;


public final class ChangeTicketStatusUseCaseTest {

    @Test
    void agent_can_change_status_open_to_in_progress() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepo = new InMemoryTicketRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepo, clock);

        User creator = new User(UserId.of(UUID.randomUUID()), "u@test.com", "U", UserRole.USER, true);
        Category category = new Category(CategoryId.of(UUID.randomUUID()), "General", true);

        Ticket ticket = Ticket.openNew("T", "D", category, creator, clock);
        ticketRepo.save(ticket);

        var cmd = new ChangeTicketStatusCommand(
                ticket.id(),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        );

        ChangeTicketStatusResult result = useCase.execute(cmd);

        assertEquals(ticket.id(), result.ticketId());
        assertEquals(TicketStatus.IN_PROGRESS, result.status());
        assertEquals(TicketStatus.IN_PROGRESS, ticketRepo.findById(ticket.id()).orElseThrow().status());
    }

    @Test
    void user_cannot_change_status() {
        Clock clock = Clock.systemUTC();

        var ticketRepo = new InMemoryTicketRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepo, clock);

        var cmd = new ChangeTicketStatusCommand(
                TicketId.of(UUID.randomUUID()),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.USER)
        );

        assertThrows(ForbiddenException.class, () -> useCase.execute(cmd));
    }

    @Test
    void throws_not_found_if_ticket_does_not_exist() {
        Clock clock = Clock.systemUTC();

        var ticketRepo = new InMemoryTicketRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepo, clock);

        var cmd = new ChangeTicketStatusCommand(
                TicketId.of(UUID.randomUUID()),
                TicketStatus.IN_PROGRESS,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        );

        assertThrows(NotFoundException.class, () -> useCase.execute(cmd));
    }

    @Test
    void invalid_transition_in_progress_to_open_is_rejected_by_domain() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepo = new InMemoryTicketRepository();
        var useCase = new ChangeTicketStatusUseCase(ticketRepo, clock);

        User creator = new User(UserId.of(UUID.randomUUID()), "u@test.com", "U", UserRole.USER, true);
        Category category = new Category(CategoryId.of(UUID.randomUUID()), "General", true);

        Ticket ticket = Ticket.openNew("T", "D", category, creator, clock);
        ticket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        ticketRepo.save(ticket);

        var cmd = new ChangeTicketStatusCommand(
                ticket.id(),
                TicketStatus.OPEN,
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.AGENT)
        );

        assertThrows(TicketInvalidStatusTransition.class, () -> useCase.execute(cmd));
    }
}
