package com.aperdigon.ticketing_backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticketingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticketing API")
                        .description("API REST del proyecto TFG Ticketing")
                        .version("v1")
                        .contact(new Contact()
                                .name("Álvaro Perdigón Gordillo"))
                        .license(new License()
                                .name("MIT")));
    }
}
