package com.aperdigon.ticketing_backend.unit.health;

import com.aperdigon.ticketing_backend.api.controller.health.HealthController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HealthControllerTest {

    @Test
    void health_returns_basic_service_metadata() {
        var response = new HealthController().health();

        assertEquals("ok", response.status());
        assertEquals("deskops-api", response.service());
        assertEquals("2026.06-demo", response.version());
    }
}
