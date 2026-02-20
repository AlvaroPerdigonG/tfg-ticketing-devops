package com.aperdigon.ticketing_backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import com.aperdigon.ticketing_backend.test_support.JwtTestTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TicketApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("ticketing_test")
            .withUsername("ticketing")
            .withPassword("ticketing");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        // Datasource -> Testcontainers Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Para tests sin Flyway: crea esquema automáticamente
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // JWT public key de TEST (no la de local)
        registry.add("spring.security.oauth2.resourceserver.jwt.public-key-location",
                () -> "classpath:keys/jwt-test-public.pem");
    }

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID AGENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CATEGORY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired
    UserSpringDataRepository userRepo;
    @Autowired
    CategorySpringDataRepository categoryRepo;

    @BeforeEach
    void seed() {
        // Category
        if (categoryRepo.findById(CATEGORY_ID).isEmpty()) {
            categoryRepo.save(new CategoryJpaEntity(CATEGORY_ID, "General", true));
        }

        // Users (ids tienen que coincidir con el sub del token)
        if (userRepo.findById(USER_ID).isEmpty()) {
            userRepo.save(new UserJpaEntity(USER_ID, "user@test.local", "Test User", UserRole.USER, true));
        }
        if (userRepo.findById(AGENT_ID).isEmpty()) {
            userRepo.save(new UserJpaEntity(AGENT_ID, "agent@test.local", "Test Agent", UserRole.AGENT, true));
        }
    }

    @Test
    void postTickets_returns201_withUserToken() throws Exception {
        String token = JwtTestTokens.token(USER_ID.toString(), List.of("USER"), 3600);

        var body = """
                {
                  "title": "Printer broken",
                  "description": "Error E23",
                  "categoryId": "%s"
                }
                """.formatted(CATEGORY_ID);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void postTickets_returns401_withoutToken() throws Exception {
        var body = """
                {
                  "title": "Printer broken",
                  "description": "Error E23",
                  "categoryId": "%s"
                }
                """.formatted(CATEGORY_ID);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchStatus_returns403_withUserToken() throws Exception {
        // 1) Crear ticket como USER
        String userToken = JwtTestTokens.token(USER_ID.toString(), List.of("USER"), 3600);

        var createBody = """
                {
                  "title": "A ticket",
                  "description": "Some description",
                  "categoryId": "%s"
                }
                """.formatted(CATEGORY_ID);

        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // 2) Intentar cambiar estado con USER -> 403
        var patchBody = """
                { "status": "IN_PROGRESS" }
                """;

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(patchBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchStatus_returns204_withAgentToken() throws Exception {
        // 1) Crear ticket como USER
        String userToken = JwtTestTokens.token(USER_ID.toString(), List.of("USER"), 3600);

        var createBody = """
                {
                  "title": "A ticket",
                  "description": "Some description",
                  "categoryId": "%s"
                }
                """.formatted(CATEGORY_ID);

        String response = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID ticketId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // 2) Cambiar estado con AGENT -> 204
        String agentToken = JwtTestTokens.token(AGENT_ID.toString(), List.of("AGENT"), 3600);

        var patchBody = """
                { "status": "IN_PROGRESS" }
                """;

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + agentToken)
                        .content(patchBody))
                .andExpect(status().isNoContent());
    }
}
