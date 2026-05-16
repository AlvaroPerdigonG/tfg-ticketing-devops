package com.aperdigon.ticketing_backend.unit.security;

import com.aperdigon.ticketing_backend.infrastructure.security.DevJwtGenerator;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DevJwtGeneratorTest {

    @Test
    void main_logs_usage_when_required_arguments_are_missing() {
        assertDoesNotThrow(() -> DevJwtGenerator.main(new String[0]));
    }

    @Test
    void main_generates_and_logs_token_when_arguments_are_valid() {
        String subject = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> DevJwtGenerator.main(new String[] {
                privateKeyPath(),
                subject,
                "AGENT"
        }));
    }

    private static String privateKeyPath() {
        Path modulePath = Path.of("src/main/resources/keys/jwt-private.pem");
        if (Files.exists(modulePath)) {
            return modulePath.toString();
        }
        return Path.of("ticketing-backend/src/main/resources/keys/jwt-private.pem").toString();
    }
}
