package com.aperdigon.ticketing_backend.integration.auth;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import com.nimbusds.jwt.SignedJWT;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthLoginIntegrationTest {

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
    PasswordEncoder passwordEncoder;

    private UUID activeUserId;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        activeUserId = UUID.randomUUID();

        userRepo.save(new UserJpaEntity(
                activeUserId,
                "user@test.com",
                "User",
                passwordEncoder.encode("secret123"),
                UserRole.USER,
                true
        ));

        userRepo.save(new UserJpaEntity(
                UUID.randomUUID(),
                "inactive@test.com",
                "Inactive",
                passwordEncoder.encode("secret123"),
                UserRole.USER,
                false
        ));
    }

    @Test
    void login_returns_jwt_when_credentials_are_valid() throws Exception {
        var mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@test.com","password":"secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        String token = body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
        SignedJWT jwt = SignedJWT.parse(token);

        assertEquals(activeUserId.toString(), jwt.getJWTClaimsSet().getSubject());
        assertEquals(List.of("USER"), jwt.getJWTClaimsSet().getStringListClaim("roles"));
        assertNotNull(jwt.getJWTClaimsSet().getExpirationTime());
    }

    @Test
    void login_returns_unauthorized_when_password_is_invalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@test.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns_unauthorized_when_user_is_inactive() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"inactive@test.com","password":"secret123"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
