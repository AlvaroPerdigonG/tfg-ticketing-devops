package com.aperdigon.ticketing_backend.unit.ticket;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketCommand;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketResult;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketUseCase;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.InMemoryCategoryRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

public final class CreateTicketUseCaseTest {

    @Test
    void creates_ticket_in_open_status_and_persists_it() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepo = new InMemoryTicketRepository();
        var categoryRepo = new InMemoryCategoryRepository();
        var userRepo = new InMemoryUserRepository();

        User creator = new User(
                UserId.of(UUID.randomUUID()),
                "user@test.com",
                "User",
                "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG",
                UserRole.USER,
                true
        );
        userRepo.put(creator);

        Category category = new Category(CategoryId.of(UUID.randomUUID()), "General", true);
        categoryRepo.put(category);

        var useCase = new CreateTicketUseCase(ticketRepo, categoryRepo, userRepo, clock);

        var cmd = new CreateTicketCommand(
                "Printer not working",
                "The printer shows error E23",
                category.id(),
                new CurrentUser(creator.id(), creator.role())
        );

        CreateTicketResult result = useCase.execute(cmd);

        Ticket saved = ticketRepo.findById(result.ticketId()).orElseThrow();
        assertEquals(TicketStatus.OPEN, saved.status());
        assertEquals(creator.id(), saved.createdBy());
        assertEquals(category.id(), saved.categoryId());
        assertEquals(Instant.parse("2026-02-14T10:00:00Z"), saved.createdAt());
        assertEquals(saved.createdAt(), saved.updatedAt());
    }

    @Test
    void fails_if_category_not_found() {
        Clock clock = Clock.systemUTC();

        var ticketRepo = new InMemoryTicketRepository();
        var categoryRepo = new InMemoryCategoryRepository();
        var userRepo = new InMemoryUserRepository();

        User creator = new User(
                UserId.of(UUID.randomUUID()),
                "user@test.com",
                "User",
                "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG",
                UserRole.USER,
                true
        );
        userRepo.put(creator);

        var useCase = new CreateTicketUseCase(ticketRepo, categoryRepo, userRepo, clock);

        var cmd = new CreateTicketCommand(
                "Title",
                "Description",
                CategoryId.of(UUID.randomUUID()),
                new CurrentUser(creator.id(), creator.role())
        );

        assertThrows(NotFoundException.class, () -> useCase.execute(cmd));
    }
}
