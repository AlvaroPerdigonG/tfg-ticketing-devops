package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.application.tickets.get.GetTicketUseCase;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GetTicketUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-USER-04", level = TestLevel.UNIT, feature = "tickets-user.feature")
    void owner_can_get_their_own_ticket_detail() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new GetTicketUseCase(ticketRepository);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("Printer issue", "Paper jam", category, owner, clock);
        ticketRepository.save(ticket);

        var result = useCase.execute(ticket.id(), new CurrentUser(owner.id(), owner.role()));

        assertSame(ticket, result);
    }

    @Test
    void non_owner_user_cannot_get_ticket_detail_from_another_user() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new GetTicketUseCase(ticketRepository);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var otherUser = DomainTestDataFactory.activeUser("other@test.com", "Other", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("Printer issue", "Paper jam", category, owner, clock);
        ticketRepository.save(ticket);

        assertThrows(ForbiddenException.class, () -> useCase.execute(ticket.id(), new CurrentUser(otherUser.id(), otherUser.role())));
    }

    @Test
    void agent_can_get_ticket_detail_created_by_another_user() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new GetTicketUseCase(ticketRepository);

        var owner = DomainTestDataFactory.activeUser("owner@test.com", "Owner", UserRole.USER);
        var agent = DomainTestDataFactory.activeUser("agent@test.com", "Agent", UserRole.AGENT);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("Printer issue", "Paper jam", category, owner, clock);
        ticketRepository.save(ticket);

        var result = useCase.execute(ticket.id(), new CurrentUser(agent.id(), agent.role()));

        assertEquals(ticket.id(), result.id());
    }

    @Test
    void throws_not_found_when_ticket_does_not_exist() {
        var useCase = new GetTicketUseCase(new InMemoryTicketRepository());

        assertThrows(NotFoundException.class, () -> useCase.execute(
                com.aperdigon.ticketing_backend.domain.ticket.TicketId.of(UUID.randomUUID()),
                new CurrentUser(com.aperdigon.ticketing_backend.domain.user.UserId.of(UUID.randomUUID()), UserRole.USER)
        ));
    }
}
