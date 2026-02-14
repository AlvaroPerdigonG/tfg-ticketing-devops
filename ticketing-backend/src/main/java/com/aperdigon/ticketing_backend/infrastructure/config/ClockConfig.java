package com.aperdigon.ticketing_backend.infrastructure.config;

import org.springframework.context.annotation.Bean;

import java.time.Clock;

public class ClockConfig {
    @Bean
    Clock clock() { return Clock.systemUTC(); }
}
