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
    public String health() {
        return "ok";
    }
}
