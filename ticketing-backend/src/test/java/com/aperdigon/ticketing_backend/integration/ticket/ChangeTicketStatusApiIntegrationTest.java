package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketEventSpringDataRepository;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangeTicketStatusApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    @Autowired
    private TicketEventSpringDataRepository ticketEventRepository;

    @BeforeEach
    void setUp() {
        clearPersistedData();
    }

    @Test
    @SpecificationRef(value = "TICKET-USER-05", level = TestLevel.INTEGRATION, feature = "tickets-user.feature")
    void user_cannot_change_ticket_status_when_request_is_blocked_by_security() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Printer issue")
                .withDescription("Paper jam")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-06T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-06T10:00:00Z"))
                .createdBy(creator)
                .inCategory(category));

        String token = loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticket.getId())
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isForbidden());

        assertEquals(TicketStatus.OPEN, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());
        assertEquals(0, ticketEventRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId()).size());
    }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-01", level = TestLevel.INTEGRATION, feature = "tickets-agent.feature")
    void agent_can_change_ticket_status_and_persistence_is_updated() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Printer issue")
                .withDescription("Paper jam")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-06T11:00:00Z"))
                .updatedAt(Instant.parse("2026-03-06T11:00:00Z"))
                .createdBy(creator)
                .inCategory(category));

        String token = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticket.getId())
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isNoContent());

        var updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        var savedEvents = ticketEventRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId());
        assertEquals(TicketStatus.IN_PROGRESS, updatedTicket.getStatus());
        assertEquals(1, savedEvents.size());
        assertEquals(TicketEventType.STATUS_CHANGED, savedEvents.get(0).getEventType());
    }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-02", level = TestLevel.INTEGRATION, feature = "tickets-agent.feature")
    void invalid_transition_returns_conflict_and_ticket_status_remains_unchanged() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Printer issue")
                .withDescription("Already in progress")
                .withStatus(TicketStatus.IN_PROGRESS)
                .createdAt(Instant.parse("2026-03-06T12:00:00Z"))
                .updatedAt(Instant.parse("2026-03-06T12:00:00Z"))
                .createdBy(creator)
                .inCategory(category));

        String token = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticket.getId())
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "OPEN"))))
                .andExpect(status().isConflict());

        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());
        assertEquals(0, ticketEventRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId()).size());
    }
}
