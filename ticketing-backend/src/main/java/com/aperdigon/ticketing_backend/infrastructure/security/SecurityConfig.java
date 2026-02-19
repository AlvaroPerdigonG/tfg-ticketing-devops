package com.aperdigon.ticketing_backend.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless: no sesiones, no cookies
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // health/actuator (ajusta a tu gusto)
                        .requestMatchers("/actuator/health", "/api/health").permitAll()

                        //UC1: crear ticket todos
                        .requestMatchers("/api/tickets").hasAnyRole("USER","AGENT","ADMIN")

                        // UC4: cambiar estado solo AGENT/ADMIN
                        .requestMatchers("/api/tickets/*/status").hasAnyRole("AGENT", "ADMIN")

                        // resto API: requiere estar autenticado
                        .requestMatchers("/api/**").authenticated()

                        // lo demás (swagger estático, etc.)
                        .anyRequest().permitAll()
                )

                // Activa Resource Server JWT (Bearer tokens)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new RolesJwtAuthenticationConverter()))
                );

        return http.build();
    }
}
