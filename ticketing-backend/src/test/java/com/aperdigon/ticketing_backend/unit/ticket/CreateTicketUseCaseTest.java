package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketCommand;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketResult;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketUseCase;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryCategoryRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketEventRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CreateTicketUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-USER-01", level = TestLevel.UNIT, feature = "tickets-user.feature")
    void creates_open_ticket_and_records_creation_event() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var categoryRepository = new InMemoryCategoryRepository();
        var userRepository = new InMemoryUserRepository();
        var ticketEventRepository = new InMemoryTicketEventRepository();

        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        userRepository.put(creator);
        categoryRepository.put(category);

        var useCase = new CreateTicketUseCase(ticketRepository, categoryRepository, userRepository, ticketEventRepository, clock);

        var command = new CreateTicketCommand(
                "Printer not working",
                "The printer shows error E23",
                category.id(),
                TicketPriority.HIGH,
                new CurrentUser(creator.id(), creator.role())
        );

        CreateTicketResult result = useCase.execute(command);

        Ticket saved = ticketRepository.findById(result.ticketId()).orElseThrow();
        var events = ticketEventRepository.findByTicketId(saved.id());

        assertEquals(TicketStatus.OPEN, saved.status());
        assertEquals(TicketPriority.HIGH, saved.priority());
        assertEquals(creator.id(), saved.createdBy());
        assertEquals(category.id(), saved.categoryId());
        assertEquals(Instant.parse("2026-02-14T10:00:00Z"), saved.createdAt());
        assertEquals(saved.createdAt(), saved.updatedAt());
        assertEquals(1, events.size());
        assertEquals(TicketEventType.TICKET_CREATED, events.get(0).type());
        assertEquals(creator.id(), events.get(0).actorUserId());
        assertEquals(Map.of(), events.get(0).payload());
        assertEquals(Instant.parse("2026-02-14T10:00:00Z"), events.get(0).createdAt());
    }

    @Test
    void rejects_ticket_creation_when_actor_user_does_not_exist() {
        var useCase = new CreateTicketUseCase(
                new InMemoryTicketRepository(),
                new InMemoryCategoryRepository(),
                new InMemoryUserRepository(),
                new InMemoryTicketEventRepository(),
                Clock.systemUTC()
        );

        var exception = assertThrows(NotFoundException.class, () -> useCase.execute(new CreateTicketCommand(
                "Title",
                "Description",
                com.aperdigon.ticketing_backend.domain.category.CategoryId.of(UUID.randomUUID()),
                TicketPriority.MEDIUM,
                new CurrentUser(com.aperdigon.ticketing_backend.domain.user.UserId.of(UUID.randomUUID()), UserRole.USER)
        )));

        assertTrue(exception.getMessage().startsWith("User not found:"));
    }

    @Test
    void rejects_ticket_creation_when_category_does_not_exist() {
        Clock clock = Clock.systemUTC();

        var ticketRepository = new InMemoryTicketRepository();
        var categoryRepository = new InMemoryCategoryRepository();
        var userRepository = new InMemoryUserRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        userRepository.put(creator);

        var useCase = new CreateTicketUseCase(ticketRepository, categoryRepository, userRepository, eventRepository, clock);

        var exception = assertThrows(NotFoundException.class, () -> useCase.execute(new CreateTicketCommand(
                "Title",
                "Description",
                com.aperdigon.ticketing_backend.domain.category.CategoryId.of(UUID.randomUUID()),
                TicketPriority.MEDIUM,
                new CurrentUser(creator.id(), creator.role())
        )));

        assertTrue(exception.getMessage().startsWith("Category not found:"));
        assertEquals(0, eventRepository.allEvents().size());
    }
}
