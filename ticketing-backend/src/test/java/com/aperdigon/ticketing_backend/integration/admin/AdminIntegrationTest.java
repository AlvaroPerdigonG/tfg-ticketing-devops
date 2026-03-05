package com.aperdigon.ticketing_backend.integration.admin;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserSpringDataRepository userRepo;

    @Autowired
    CategorySpringDataRepository categoryRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    private UUID managedUserId;

    @BeforeEach
    void setUp() {
        categoryRepo.deleteAll();
        userRepo.deleteAll();

        userRepo.save(new UserJpaEntity(
                UUID.randomUUID(),
                "admin@test.com",
                "Admin",
                passwordEncoder.encode("secret123"),
                UserRole.ADMIN,
                true
        ));

        managedUserId = UUID.randomUUID();
        userRepo.save(new UserJpaEntity(
                managedUserId,
                "user@test.com",
                "Regular User",
                passwordEncoder.encode("secret123"),
                UserRole.USER,
                true
        ));

        categoryRepo.save(new CategoryJpaEntity(UUID.randomUUID(), "Software", true));
    }

    @Test
    void admin_can_list_categories_and_users() throws Exception {
        String token = loginAndExtractToken("admin@test.com", "secret123");

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Software"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='user@test.com')]").exists());
    }

    @Test
    void admin_can_deactivate_user() throws Exception {
        String token = loginAndExtractToken("admin@test.com", "secret123");

        mockMvc.perform(patch("/api/admin/users/{userId}/active", managedUserId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isActive":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@test.com","password":"secret123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void non_admin_cannot_access_admin_endpoints() throws Exception {
        String token = loginAndExtractToken("user@test.com", "secret123");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        var mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        return body.replaceAll(".*\\\"accessToken\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
