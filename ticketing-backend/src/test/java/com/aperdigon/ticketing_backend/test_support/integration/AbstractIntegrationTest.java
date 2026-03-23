package com.aperdigon.ticketing_backend.test_support.integration;

import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketSpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketEventSpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.UserTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserSpringDataRepository userRepository;

    @Autowired
    protected CategorySpringDataRepository categoryRepository;

    @Autowired
    protected TicketSpringDataRepository ticketRepository;

    @Autowired
    protected TicketEventSpringDataRepository ticketEventRepository;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    protected void clearPersistedData() {
        ticketEventRepository.deleteAll();
        ticketRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected UserJpaEntity persistUser(UserTestDataBuilder builder) {
        return userRepository.save(builder.build(passwordEncoder));
    }

    protected CategoryJpaEntity persistCategory(CategoryTestDataBuilder builder) {
        return categoryRepository.save(builder.build());
    }

    protected TicketJpaEntity persistTicket(TicketTestDataBuilder builder) {
        return ticketRepository.save(builder.build());
    }

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected ResultActions postJson(String urlTemplate, Object body, Object... uriVariables) throws Exception {
        return mockMvc.perform(post(urlTemplate, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }
}
