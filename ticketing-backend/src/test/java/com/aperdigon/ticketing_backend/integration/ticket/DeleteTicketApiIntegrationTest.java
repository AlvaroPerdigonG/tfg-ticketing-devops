package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeleteTicketApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {
    private static final String DEFAULT_PASSWORD = "secret123";

    @BeforeEach
    void setUp() { clearPersistedData(); }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-05", level = TestLevel.INTEGRATION, feature = "tickets-agent.feature")
    void admin_can_delete_ticket() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("admin@test.com", "Admin", DEFAULT_PASSWORD, UserRole.ADMIN);
        var category = persistCategory(CategoryTestDataBuilder.aCategory());
        var ticket = persistTicket(TicketTestDataBuilder.aTicket().createdBy(creator).inCategory(category));
        var token = loginAndExtractAccessToken("admin@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(delete("/api/tickets/{id}", ticket.getId()).with(bearerToken(token))).andExpect(status().isNoContent());

        assertFalse(ticketRepository.findById(ticket.getId()).isPresent());
    }

    @Test
    void user_and_agent_cannot_delete_ticket() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var category = persistCategory(CategoryTestDataBuilder.aCategory());
        var ticket = persistTicket(TicketTestDataBuilder.aTicket().createdBy(creator).inCategory(category));

        mockMvc.perform(delete("/api/tickets/{id}", ticket.getId()).with(bearerToken(loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD)))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/tickets/{id}", ticket.getId()).with(bearerToken(loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD)))).andExpect(status().isForbidden());
    }

    @Test
    void deleting_non_existing_ticket_returns_not_found() throws Exception {
        persistActiveUser("admin@test.com", "Admin", DEFAULT_PASSWORD, UserRole.ADMIN);
        mockMvc.perform(delete("/api/tickets/{id}", UUID.randomUUID()).with(bearerToken(loginAndExtractAccessToken("admin@test.com", DEFAULT_PASSWORD)))).andExpect(status().isNotFound());
    }
}
