package com.aperdigon.ticketing_backend.unit.auth;

import com.aperdigon.ticketing_backend.application.auth.register.RegisterCommand;
import com.aperdigon.ticketing_backend.application.auth.register.RegisterUseCase;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUseCaseTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void creates_user_and_returns_token_when_payload_is_valid() {
        var repo = new InMemoryUserRepository();
        var useCase = new RegisterUseCase(repo, encoder, u -> "jwt-token");

        var result = useCase.execute(new RegisterCommand(
                "user@test.com",
                "User",
                "Secret123!",
                "Secret123!"
        ));

        assertEquals("jwt-token", result.accessToken());
        var savedUser = repo.findByEmail("user@test.com").orElseThrow();
        assertEquals(UserRole.USER, savedUser.role());
        assertTrue(savedUser.isActive());
        assertNotEquals("Secret123!", savedUser.passwordHash());
        assertTrue(encoder.matches("Secret123!", savedUser.passwordHash()));
    }

    @Test
    void throws_when_passwords_do_not_match() {
        var useCase = new RegisterUseCase(new InMemoryUserRepository(), encoder, u -> "token");

        assertThrows(InvalidArgumentException.class, () -> useCase.execute(new RegisterCommand(
                "user@test.com",
                "User",
                "Secret123!",
                "Different123!"
        )));
    }

    @Test
    void throws_when_password_does_not_meet_policy() {
        var useCase = new RegisterUseCase(new InMemoryUserRepository(), encoder, u -> "token");

        assertThrows(InvalidArgumentException.class, () -> useCase.execute(new RegisterCommand(
                "user@test.com",
                "User",
                "weakpass",
                "weakpass"
        )));
    }

    @Test
    void throws_generic_error_when_email_already_exists() {
        var repo = new InMemoryUserRepository();
        var useCase = new RegisterUseCase(repo, encoder, u -> "token");

        useCase.execute(new RegisterCommand("user@test.com", "User", "Secret123!", "Secret123!"));

        var ex = assertThrows(InvalidArgumentException.class, () -> useCase.execute(new RegisterCommand(
                "user@test.com",
                "User 2",
                "Secret123!",
                "Secret123!"
        )));

        assertEquals("Registration failed", ex.getMessage());
    }
}
