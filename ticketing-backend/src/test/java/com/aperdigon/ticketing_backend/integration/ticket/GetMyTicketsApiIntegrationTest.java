package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetMyTicketsApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        clearPersistedData();
    }

    @Test
    @SpecificationRef(value = "TICKET-USER-03", level = TestLevel.INTEGRATION, feature = "tickets-user.feature")
    void user_sees_only_their_own_tickets() throws Exception {
        var requester = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var anotherUser = persistActiveUser("other@test.com", "Other User", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("My open ticket")
                .withDescription("Visible in my list")
                .withPriority(TicketPriority.HIGH)
                .createdAt(Instant.parse("2026-03-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-01T10:00:00Z"))
                .createdBy(requester)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Another of my tickets")
                .withDescription("Also visible in my list")
                .withPriority(TicketPriority.MEDIUM)
                .createdAt(Instant.parse("2026-03-01T09:00:00Z"))
                .updatedAt(Instant.parse("2026-03-01T09:30:00Z"))
                .createdBy(requester)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Other user ticket")
                .withDescription("Should not be visible")
                .withPriority(TicketPriority.LOW)
                .createdAt(Instant.parse("2026-03-02T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-02T10:00:00Z"))
                .createdBy(anotherUser)
                .inCategory(category));

        String token = loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets/me")
                        .with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[?(@.title=='My open ticket')]").exists())
                .andExpect(jsonPath("$.items[?(@.title=='Another of my tickets')]").exists())
                .andExpect(jsonPath("$.items[?(@.title=='Other user ticket')]").doesNotExist());
    }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-03", level = TestLevel.INTEGRATION, feature = "tickets-agent.feature")
    void agent_can_list_manageable_tickets_using_operational_scope_filters() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var agent = persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var otherAgent = persistActiveUser("other.agent@test.com", "Other Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Assigned to me")
                .withDescription("Hardware queue item")
                .withStatus(TicketStatus.IN_PROGRESS)
                .createdAt(Instant.parse("2026-03-03T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-03T11:00:00Z"))
                .createdBy(creator)
                .assignedTo(agent)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Assigned to another agent")
                .withDescription("Should not match MINE scope")
                .withStatus(TicketStatus.IN_PROGRESS)
                .createdAt(Instant.parse("2026-03-03T09:00:00Z"))
                .updatedAt(Instant.parse("2026-03-03T12:00:00Z"))
                .createdBy(creator)
                .assignedTo(otherAgent)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Unassigned queue ticket")
                .withDescription("Still open")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-02T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-02T10:00:00Z"))
                .createdBy(creator)
                .inCategory(category));

        String token = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets")
                        .with(bearerToken(token))
                        .param("scope", "MINE")
                        .param("status", "IN_PROGRESS")
                        .param("q", "assigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Assigned to me"))
                .andExpect(jsonPath("$.items[0].assignedToUserId").value(agent.getId().toString()));
    }
}
