package com.aperdigon.ticketing_backend.integration.admin;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    private UUID managedUserId;

    @BeforeEach
    void setUp() {
        clearPersistedData();

        persistActiveUser("admin@test.com", "Admin", DEFAULT_PASSWORD, UserRole.ADMIN);

        managedUserId = persistActiveUser("user@test.com", "Regular User", DEFAULT_PASSWORD, UserRole.USER).getId();

        persistCategory(CategoryTestDataBuilder.aCategory().withName("Software"));
    }

    @Test
    @SpecificationRef(value = "ADMIN-02", level = TestLevel.INTEGRATION, feature = "admin.feature")
    void admin_can_list_categories() throws Exception {
        mockMvc.perform(get("/api/admin/categories")
                        .with(authenticatedAs("admin@test.com", DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Software"));
    }

    @Test
    @SpecificationRef(value = "ADMIN-01", level = TestLevel.INTEGRATION, feature = "admin.feature")
    void admin_can_list_users() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(authenticatedAs("admin@test.com", DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='user@test.com')]").exists());
    }

    @Test
    @SpecificationRef(value = "ADMIN-03", level = TestLevel.INTEGRATION, feature = "admin.feature")
    void admin_can_deactivate_user() throws Exception {
        mockMvc.perform(patchJson("/api/admin/users/{userId}/active", Map.of("isActive", false), managedUserId)
                        .with(authenticatedAs("admin@test.com", DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        postJson("/api/auth/login", Map.of(
                "email", "user@test.com",
                "password", DEFAULT_PASSWORD
        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SpecificationRef(value = "ADMIN-04", level = TestLevel.INTEGRATION, feature = "admin.feature")
    void non_admin_cannot_access_admin_endpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(authenticatedAs("user@test.com", DEFAULT_PASSWORD)))
                .andExpect(status().isForbidden());
    }
}
