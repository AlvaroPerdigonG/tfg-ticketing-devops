package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.tickets.detail.GetTicketDetailUseCase;
import com.aperdigon.ticketing_backend.application.tickets.get.GetTicketUseCase;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
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

final class GetTicketDetailUseCaseTest {

    @Test
    void returns_ticket_events_and_display_names_for_related_users() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var userRepository = new InMemoryUserRepository();

        var creator = DomainTestDataFactory.activeUser("creator@test.com", "Creator", UserRole.USER);
        var agent = DomainTestDataFactory.activeUser("agent@test.com", "Agent", UserRole.AGENT);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);
        ticket.assignTo(agent.id(), clock);
        ticket.addComment("I am checking it", agent.id(), clock);
        ticketRepository.save(ticket);
        userRepository.put(creator);
        userRepository.put(agent);
        var event = new TicketEvent(
                UUID.randomUUID(),
                ticket.id(),
                TicketEventType.ASSIGNED_TO_ME,
                agent.id(),
                Map.of(),
                Instant.parse("2026-04-01T09:00:00Z")
        );
        eventRepository.save(event);
        var useCase = new GetTicketDetailUseCase(new GetTicketUseCase(ticketRepository), eventRepository, userRepository);

        var result = useCase.execute(ticket.id(), new CurrentUser(creator.id(), creator.role()));

        assertEquals(ticket.id(), result.ticket().id());
        assertEquals(1, result.events().size());
        assertEquals(event, result.events().get(0));
        assertEquals("Creator", result.resolveUserDisplayName(creator.id().value()));
        assertEquals("Agent", result.resolveUserDisplayName(agent.id().value()));
    }

    @Test
    void preserves_get_ticket_access_rules_for_non_owner_users() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var eventRepository = new InMemoryTicketEventRepository();
        var userRepository = new InMemoryUserRepository();
        var creator = DomainTestDataFactory.activeUser("creator@test.com", "Creator", UserRole.USER);
        var otherUser = DomainTestDataFactory.activeUser("other@test.com", "Other", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");
        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);
        ticketRepository.save(ticket);
        var useCase = new GetTicketDetailUseCase(new GetTicketUseCase(ticketRepository), eventRepository, userRepository);

        assertThrows(ForbiddenException.class, () -> useCase.execute(ticket.id(), new CurrentUser(otherUser.id(), otherUser.role())));
    }
}
