package com.aperdigon.ticketing_backend.unit.auth;

import com.aperdigon.ticketing_backend.application.auth.register.RegisterCommand;
import com.aperdigon.ticketing_backend.application.auth.register.RegisterUseCase;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterUseCaseTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @SpecificationRef(value = "AUTH-04", level = TestLevel.UNIT, feature = "authentication.feature")
    void registers_user_with_default_role_and_returns_token_for_saved_user() {
        var users = new InMemoryUserRepository();
        AtomicReference<String> issuedForEmail = new AtomicReference<>();
        var useCase = new RegisterUseCase(users, encoder, user -> {
            issuedForEmail.set(user.email());
            return "jwt-token";
        });

        var result = useCase.execute(new RegisterCommand(
                "  USER@Test.com  ",
                "  New User  ",
                "Secret123!",
                "Secret123!"
        ));

        var savedUser = users.findByEmail("user@test.com").orElseThrow();
        assertEquals("jwt-token", result.accessToken());
        assertEquals("user@test.com", savedUser.email());
        assertEquals("New User", savedUser.displayName());
        assertEquals(UserRole.USER, savedUser.role());
        assertTrue(savedUser.isActive());
        assertNotEquals("Secret123!", savedUser.passwordHash());
        assertTrue(encoder.matches("Secret123!", savedUser.passwordHash()));
        assertEquals("user@test.com", issuedForEmail.get());
    }

    @Test
    void rejects_registration_when_passwords_do_not_match() {
        var users = new InMemoryUserRepository();
        AtomicBoolean tokenIssued = new AtomicBoolean(false);
        var useCase = new RegisterUseCase(users, encoder, user -> {
            tokenIssued.set(true);
            return "token";
        });

        assertThrows(InvalidArgumentException.class, () -> useCase.execute(new RegisterCommand(
                "user@test.com",
                "User",
                "Secret123!",
                "Different123!"
        )));
        assertTrue(users.findAll().isEmpty());
        assertFalse(tokenIssued.get());
    }

    @Test
    void rejects_registration_when_password_does_not_meet_policy() {
        var users = new InMemoryUserRepository();
        var useCase = new RegisterUseCase(users, encoder, user -> "token");

        var exception = assertThrows(InvalidArgumentException.class, () -> useCase.execute(new RegisterCommand(
                "user@test.com",
                "User",
                "weakpass",
                "weakpass"
        )));

        assertEquals("Password must have at least 8 characters, including uppercase, lowercase, number and symbol", exception.getMessage());
        assertTrue(users.findAll().isEmpty());
    }

    @Test
    @SpecificationRef(value = "AUTH-05", level = TestLevel.UNIT, feature = "authentication.feature")
    void rejects_registration_when_email_already_exists_case_insensitively() {
        var users = new InMemoryUserRepository();
        users.put(DomainTestDataFactory.activeUser("user@test.com", "Existing User", UserRole.USER));
        var useCase = new RegisterUseCase(users, encoder, user -> "token");

        var exception = assertThrows(InvalidArgumentException.class, () -> useCase.execute(new RegisterCommand(
                "USER@Test.com",
                "Duplicate User",
                "Secret123!",
                "Secret123!"
        )));

        assertEquals("Registration failed", exception.getMessage());
        assertEquals(1, users.findAll().size());
    }
}
