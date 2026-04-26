package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetDashboardStatsApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        clearPersistedData();
    }

    @Test
    void agent_gets_dashboard_stats_cards_and_charts() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var agent = persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var admin = persistActiveUser("admin@test.com", "Admin", DEFAULT_PASSWORD, UserRole.ADMIN);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Unassigned open")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-13T09:00:00Z"))
                .updatedAt(Instant.parse("2026-03-13T09:00:00Z"))
                .createdBy(creator)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Assigned in progress")
                .withStatus(TicketStatus.IN_PROGRESS)
                .createdAt(Instant.parse("2026-03-13T09:10:00Z"))
                .updatedAt(Instant.parse("2026-03-13T09:10:00Z"))
                .createdBy(creator)
                .assignedTo(agent)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Assigned on hold")
                .withStatus(TicketStatus.ON_HOLD)
                .createdAt(Instant.parse("2026-03-13T09:20:00Z"))
                .updatedAt(Instant.parse("2026-03-13T09:20:00Z"))
                .createdBy(creator)
                .assignedTo(agent)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Resolved by agent")
                .withStatus(TicketStatus.RESOLVED)
                .createdAt(Instant.parse("2026-03-13T09:30:00Z"))
                .updatedAt(Instant.parse("2026-03-13T09:30:00Z"))
                .createdBy(creator)
                .assignedTo(agent)
                .inCategory(category));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Resolved by admin")
                .withStatus(TicketStatus.RESOLVED)
                .createdAt(Instant.parse("2026-03-13T09:40:00Z"))
                .updatedAt(Instant.parse("2026-03-13T09:40:00Z"))
                .createdBy(creator)
                .assignedTo(admin)
                .inCategory(category));

        String token = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets/dashboard/stats")
                        .with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.unassigned").value(1))
                .andExpect(jsonPath("$.cards.assignedToMe").value(3))
                .andExpect(jsonPath("$.cards.inProgress").value(1))
                .andExpect(jsonPath("$.cards.onHold").value(1))
                .andExpect(jsonPath("$.charts.resolvedByAgent[0].count").value(1))
                .andExpect(jsonPath("$.charts.assignedByAgent[0].count").value(3));
    }

    @Test
    void admin_can_access_dashboard_stats() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var admin = persistActiveUser("admin@test.com", "Admin", DEFAULT_PASSWORD, UserRole.ADMIN);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));

        persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Assigned to admin")
                .withStatus(TicketStatus.IN_PROGRESS)
                .createdAt(Instant.parse("2026-03-13T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-13T10:00:00Z"))
                .createdBy(creator)
                .assignedTo(admin)
                .inCategory(category));

        String token = loginAndExtractAccessToken("admin@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets/dashboard/stats")
                        .with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.assignedToMe").value(1));
    }

    @Test
    void user_cannot_access_dashboard_stats() throws Exception {
        persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        String token = loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets/dashboard/stats")
                        .with(bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void missing_token_returns_unauthorized() throws Exception {
        mockMvc.perform(get("/api/tickets/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
