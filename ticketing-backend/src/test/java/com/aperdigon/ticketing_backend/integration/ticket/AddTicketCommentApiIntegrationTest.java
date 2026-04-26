package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddTicketCommentApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        clearPersistedData();
    }

    @Test
    void owner_can_add_comment_and_comment_is_visible_in_ticket_detail_timeline() throws Exception {
        var owner = persistActiveUser("owner@test.com", "Owner", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Printer issue")
                .withDescription("Paper jam")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-12T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-12T10:00:00Z"))
                .createdBy(owner)
                .inCategory(category));

        String token = loginAndExtractAccessToken("owner@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/tickets/{id}/comments", ticket.getId())
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Necesito ayuda con urgencia"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/tickets/.+/comments/.+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.ticketId").value(ticket.getId().toString()))
                .andExpect(jsonPath("$.authorUserId").value(owner.getId().toString()))
                .andExpect(jsonPath("$.content").value("Necesito ayuda con urgencia"));

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline[?(@.kind=='MESSAGE' && @.content=='Necesito ayuda con urgencia')]").exists());
    }

    @Test
    void agent_can_add_comment_to_foreign_ticket() throws Exception {
        var owner = persistActiveUser("owner@test.com", "Owner", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Network issue")
                .withDescription("Switch offline")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-12T10:30:00Z"))
                .updatedAt(Instant.parse("2026-03-12T10:30:00Z"))
                .createdBy(owner)
                .inCategory(category));

        String agentToken = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/tickets/{id}/comments", ticket.getId())
                        .with(bearerToken(agentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Lo reviso ahora mismo"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Lo reviso ahora mismo"));
    }

    @Test
    void unrelated_user_gets_forbidden_when_trying_to_comment_foreign_ticket() throws Exception {
        var owner = persistActiveUser("owner@test.com", "Owner", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("viewer@test.com", "Viewer", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Private issue")
                .withDescription("Only owner should comment")
                .createdAt(Instant.parse("2026-03-12T11:00:00Z"))
                .updatedAt(Instant.parse("2026-03-12T11:00:00Z"))
                .createdBy(owner)
                .inCategory(category));

        String viewerToken = loginAndExtractAccessToken("viewer@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/tickets/{id}/comments", ticket.getId())
                        .with(bearerToken(viewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "No debería poder"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void resolved_ticket_rejects_new_comments() throws Exception {
        var owner = persistActiveUser("owner@test.com", "Owner", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Resolved issue")
                .withDescription("Already solved")
                .withStatus(TicketStatus.RESOLVED)
                .createdAt(Instant.parse("2026-03-12T11:30:00Z"))
                .updatedAt(Instant.parse("2026-03-12T11:30:00Z"))
                .createdBy(owner)
                .inCategory(category));

        String ownerToken = loginAndExtractAccessToken("owner@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/tickets/{id}/comments", ticket.getId())
                        .with(bearerToken(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Comentario tardío"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void commenting_non_existing_ticket_returns_not_found() throws Exception {
        persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        String agentToken = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/tickets/{id}/comments", UUID.randomUUID())
                        .with(bearerToken(agentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Comentario"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void missing_token_returns_unauthorized() throws Exception {
        var owner = persistActiveUser("owner@test.com", "Owner", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .createdBy(owner)
                .inCategory(category));

        mockMvc.perform(post("/api/tickets/{id}/comments", ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Sin token"))))
                .andExpect(status().isUnauthorized());
    }
}
