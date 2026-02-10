package com.aperdigon.ticketing_backend.api.controller.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class HealthController {

    @GetMapping("/api/health")
    public String health() {
        return "ok";
    }
}

