package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.add_comment.AddTicketCommentCommand;
import com.aperdigon.ticketing_backend.application.tickets.add_comment.AddTicketCommentUseCase;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AddTicketCommentUseCaseTest {

    @Test
    void owner_can_add_comment_to_their_ticket() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T09:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new AddTicketCommentUseCase(ticketRepository, clock);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("Hardware");
        var ticket = DomainTestDataFactory.openTicket("Printer issue", "Paper jam", category, owner, clock);
        ticketRepository.save(ticket);

        var result = useCase.execute(new AddTicketCommentCommand(
                ticket.id(),
                "Necesito ayuda urgente",
                new CurrentUser(owner.id(), UserRole.USER)
        ));

        var savedTicket = ticketRepository.findById(ticket.id()).orElseThrow();
        assertEquals(ticket.id(), result.ticketId());
        assertEquals(owner.id(), result.authorUserId());
        assertEquals("Necesito ayuda urgente", result.content());
        assertEquals(1, savedTicket.comments().size());
        assertEquals("Necesito ayuda urgente", savedTicket.comments().get(0).content());
    }

    @Test
    void agent_can_add_comment_to_foreign_ticket() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T09:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new AddTicketCommentUseCase(ticketRepository, clock);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("Hardware");
        var ticket = DomainTestDataFactory.openTicket("Network issue", "Switch offline", category, owner, clock);
        ticketRepository.save(ticket);

        UserId agentId = UserId.of(UUID.randomUUID());

        var result = useCase.execute(new AddTicketCommentCommand(
                ticket.id(),
                "Lo estoy revisando",
                new CurrentUser(agentId, UserRole.AGENT)
        ));

        var savedTicket = ticketRepository.findById(ticket.id()).orElseThrow();
        assertEquals(agentId, result.authorUserId());
        assertEquals(1, savedTicket.comments().size());
        assertEquals("Lo estoy revisando", savedTicket.comments().get(0).content());
    }

    @Test
    void unrelated_user_cannot_comment_ticket() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T09:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new AddTicketCommentUseCase(ticketRepository, clock);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("Hardware");
        var ticket = DomainTestDataFactory.openTicket("Laptop issue", "Blue screen", category, owner, clock);
        ticketRepository.save(ticket);

        assertThrows(ForbiddenException.class, () -> useCase.execute(new AddTicketCommentCommand(
                ticket.id(),
                "No debería poder comentar",
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.USER)
        )));

        assertEquals(0, ticketRepository.findById(ticket.id()).orElseThrow().comments().size());
    }

    @Test
    void resolved_ticket_does_not_accept_new_comments() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T09:00:00Z"), ZoneOffset.UTC);

        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new AddTicketCommentUseCase(ticketRepository, clock);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("Hardware");
        var ticket = DomainTestDataFactory.openTicket("Mouse issue", "Disconnected", category, owner, clock);
        ticket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        ticket.changeStatus(TicketStatus.RESOLVED, clock);
        ticketRepository.save(ticket);

        assertThrows(ForbiddenException.class, () -> useCase.execute(new AddTicketCommentCommand(
                ticket.id(),
                "Intento posterior al cierre",
                new CurrentUser(owner.id(), UserRole.USER)
        )));

        assertEquals(0, ticketRepository.findById(ticket.id()).orElseThrow().comments().size());
    }

    @Test
    void throws_not_found_when_ticket_does_not_exist() {
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new AddTicketCommentUseCase(ticketRepository, Clock.systemUTC());

        assertThrows(NotFoundException.class, () -> useCase.execute(new AddTicketCommentCommand(
                com.aperdigon.ticketing_backend.domain.ticket.TicketId.of(UUID.randomUUID()),
                "Comentario",
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN)
        )));
    }

    @Test
    void null_command_is_rejected() {
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new AddTicketCommentUseCase(ticketRepository, Clock.systemUTC());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }
}
