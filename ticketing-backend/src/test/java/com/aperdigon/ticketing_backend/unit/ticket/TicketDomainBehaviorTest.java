package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketAlreadyResolved;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TicketDomainBehaviorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-01T09:00:00Z"), ZoneOffset.UTC);

    @Test
    void open_new_ticket_sets_initial_state_and_defaults() {
        var category = DomainTestDataFactory.activeCategory("General");
        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);

        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);

        assertNotNull(ticket.id());
        assertEquals("Printer", ticket.title());
        assertEquals("Printer is offline", ticket.description());
        assertEquals(category.id(), ticket.categoryId());
        assertEquals(creator.id(), ticket.createdBy());
        assertEquals(TicketStatus.OPEN, ticket.status());
        assertEquals(TicketPriority.MEDIUM, ticket.priority());
        assertEquals(Instant.parse("2026-04-01T09:00:00Z"), ticket.createdAt());
        assertEquals(ticket.createdAt(), ticket.updatedAt());
        assertEquals(0, ticket.comments().size());
    }

    @Test
    void add_comment_records_author_timestamp_and_updates_ticket() {
        var category = DomainTestDataFactory.activeCategory("General");
        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);

        var comment = ticket.addComment("I can reproduce it", creator.id(), clock);

        assertNotNull(comment.id());
        assertEquals("I can reproduce it", comment.content());
        assertEquals(creator.id(), comment.authorId());
        assertEquals(Instant.parse("2026-04-01T09:00:00Z"), comment.createdAt());
        assertEquals(1, ticket.comments().size());
        assertEquals(comment, ticket.comments().get(0));
        assertEquals(Instant.parse("2026-04-01T09:00:00Z"), ticket.updatedAt());
    }

    @Test
    void add_comment_rejects_blank_content_and_null_author() {
        var category = DomainTestDataFactory.activeCategory("General");
        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);

        assertThrows(InvalidArgumentException.class, () -> ticket.addComment("  ", creator.id(), clock));
        assertThrows(InvalidArgumentException.class, () -> ticket.addComment("valid", null, clock));
        assertTrue(ticket.comments().isEmpty());
    }

    @Test
    void assign_to_agent_id_updates_assignee_and_updated_at() {
        var category = DomainTestDataFactory.activeCategory("General");
        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var agent = DomainTestDataFactory.activeUser("agent@test.com", "Agent", UserRole.AGENT);
        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);
        var assignmentClock = Clock.fixed(Instant.parse("2026-04-01T10:00:00Z"), ZoneOffset.UTC);

        ticket.assignTo(agent.id(), assignmentClock);

        assertEquals(agent.id(), ticket.assignedTo());
        assertEquals(Instant.parse("2026-04-01T10:00:00Z"), ticket.updatedAt());
    }

    @Test
    void resolved_ticket_is_terminal() {
        var category = DomainTestDataFactory.activeCategory("General");
        var creator = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        var ticket = DomainTestDataFactory.openTicket("Printer", "Printer is offline", category, creator, clock);
        ticket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        ticket.changeStatus(TicketStatus.RESOLVED, clock);

        assertThrows(TicketAlreadyResolved.class, () -> ticket.changeStatus(TicketStatus.IN_PROGRESS, clock));
        assertEquals(TicketStatus.RESOLVED, ticket.status());
    }
}
