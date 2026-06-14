package com.aperdigon.ticketing_backend.api.controller.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Application health endpoint")
public final class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Health check")
    public HealthResponse health() {
        return new HealthResponse("ok", "deskops-api", "2026.06-demo");
    }

    public record HealthResponse(String status, String service, String version) {}
}
