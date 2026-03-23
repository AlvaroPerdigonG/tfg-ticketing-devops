package com.aperdigon.ticketing_backend.integration.auth;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.UserTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    private UUID activeUserId;

    @BeforeEach
    void setUp() {
        clearPersistedData();

        activeUserId = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER).getId();

        persistUser(UserTestDataBuilder.aUser()
                .withEmail("inactive@test.com")
                .withDisplayName("Inactive")
                .withPassword(DEFAULT_PASSWORD)
                .withRole(UserRole.USER)
                .inactive());
    }

    @Test
    void login_preflight_returns_cors_headers_for_frontend_origin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @SpecificationRef(value = "AUTH-01", level = TestLevel.INTEGRATION, feature = "authentication.feature")
    void login_returns_jwt_when_credentials_are_valid() throws Exception {
        var mvcResult = postJson("/api/auth/login", Map.of(
                "email", "user@test.com",
                "password", DEFAULT_PASSWORD
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn();

        String token = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("accessToken").asText();
        SignedJWT jwt = SignedJWT.parse(token);

        assertEquals(activeUserId.toString(), jwt.getJWTClaimsSet().getSubject());
        assertEquals("user@test.com", jwt.getJWTClaimsSet().getStringClaim("email"));
        assertEquals("User", jwt.getJWTClaimsSet().getStringClaim("displayName"));
        assertEquals(List.of("USER"), jwt.getJWTClaimsSet().getStringListClaim("roles"));
        assertNotNull(jwt.getJWTClaimsSet().getExpirationTime());
    }

    @Test
    @SpecificationRef(value = "AUTH-02", level = TestLevel.INTEGRATION, feature = "authentication.feature", note = "Covers invalid login with wrong password.")
    void login_returns_unauthorized_when_password_is_invalid() throws Exception {
        postJson("/api/auth/login", Map.of(
                "email", "user@test.com",
                "password", "wrong"
        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SpecificationRef(value = "AUTH-03", level = TestLevel.INTEGRATION, feature = "authentication.feature")
    void login_returns_unauthorized_when_user_is_inactive() throws Exception {
        postJson("/api/auth/login", Map.of(
                "email", "inactive@test.com",
                "password", DEFAULT_PASSWORD
        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SpecificationRef(value = "AUTH-04", level = TestLevel.INTEGRATION, feature = "authentication.feature")
    void register_creates_user_and_returns_jwt() throws Exception {
        var mvcResult = postJson("/api/auth/register", Map.of(
                "email", "new.user@test.com",
                "displayName", "New User",
                "password", "Secret123!",
                "confirmPassword", "Secret123!"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn();

        String token = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("accessToken").asText();
        SignedJWT jwt = SignedJWT.parse(token);

        assertEquals(List.of("USER"), jwt.getJWTClaimsSet().getStringListClaim("roles"));
        assertNotNull(jwt.getJWTClaimsSet().getExpirationTime());

        var savedUser = userRepository.findByEmailIgnoreCase("new.user@test.com").orElseThrow();
        assertEquals(UserRole.USER, savedUser.getRole());
        assertEquals("New User", savedUser.getDisplayName());
    }

    @Test
    void me_returns_profile_data_from_jwt_claims() throws Exception {
        String token = loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/auth/me")
                        .with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value(activeUserId.toString()))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.displayName").value("User"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @SpecificationRef(value = "AUTH-05", level = TestLevel.INTEGRATION, feature = "authentication.feature")
    void register_returns_bad_request_when_email_is_duplicated() throws Exception {
        postJson("/api/auth/register", Map.of(
                "email", "user@test.com",
                "displayName", "Duplicate User",
                "password", "Secret123!",
                "confirmPassword", "Secret123!"
        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration failed"));
    }

    @Test
    void register_returns_bad_request_when_password_does_not_match_policy() throws Exception {
        postJson("/api/auth/register", Map.of(
                "email", "weak.user@test.com",
                "displayName", "Weak User",
                "password", "password",
                "confirmPassword", "password"
        ))
                .andExpect(status().isBadRequest());
    }
}
