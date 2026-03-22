package com.aperdigon.ticketing_backend.test_support.integration;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.test_support.builders.UserTestDataBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractAuthenticatedApiIntegrationTest extends AbstractIntegrationTest {

    protected UserJpaEntity persistActiveUser(String email, String displayName, String password, UserRole role) {
        return persistUser(UserTestDataBuilder.aUser()
                .withEmail(email)
                .withDisplayName(displayName)
                .withPassword(password)
                .withRole(role)
                .active());
    }

    protected String loginAndExtractAccessToken(String email, String password) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    protected RequestPostProcessor bearerToken(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    protected RequestPostProcessor authenticatedAs(String email, String password) throws Exception {
        return bearerToken(loginAndExtractAccessToken(email, password));
    }
}
