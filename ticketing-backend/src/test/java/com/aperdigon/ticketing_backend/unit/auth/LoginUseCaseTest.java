package com.aperdigon.ticketing_backend.unit.auth;

import com.aperdigon.ticketing_backend.application.auth.login.LoginCommand;
import com.aperdigon.ticketing_backend.application.auth.login.LoginUseCase;
import com.aperdigon.ticketing_backend.application.shared.exception.UnauthorizedException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginUseCaseTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @SpecificationRef(value = "AUTH-01", level = TestLevel.UNIT, feature = "authentication.feature")
    void authenticates_active_user_and_issues_token_using_normalized_email() {
        var users = new InMemoryUserRepository();
        var registeredUser = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        users.put(new com.aperdigon.ticketing_backend.domain.user.User(
                registeredUser.id(),
                registeredUser.email(),
                registeredUser.displayName(),
                encoder.encode("secret123"),
                registeredUser.role(),
                registeredUser.isActive()
        ));

        AtomicReference<String> issuedTokenForEmail = new AtomicReference<>();
        var useCase = new LoginUseCase(users, encoder, user -> {
            issuedTokenForEmail.set(user.email());
            return "jwt-token";
        });

        var result = useCase.execute(new LoginCommand("  USER@Test.com  ", "secret123"));

        assertEquals("jwt-token", result.accessToken());
        assertEquals("user@test.com", issuedTokenForEmail.get());
    }

    @Test
    @SpecificationRef(value = "AUTH-02", level = TestLevel.UNIT, feature = "authentication.feature", note = "Covers invalid login with unknown email.")
    void rejects_login_when_email_does_not_exist() {
        AtomicBoolean tokenIssued = new AtomicBoolean(false);
        var useCase = new LoginUseCase(new InMemoryUserRepository(), encoder, user -> {
            tokenIssued.set(true);
            return "token";
        });

        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("missing@test.com", "secret123")));
        assertFalse(tokenIssued.get());
    }

    @Test
    @SpecificationRef(value = "AUTH-02", level = TestLevel.UNIT, feature = "authentication.feature", note = "Covers invalid login with wrong password.")
    void rejects_login_when_password_is_invalid() {
        var users = new InMemoryUserRepository();
        var registeredUser = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        users.put(new com.aperdigon.ticketing_backend.domain.user.User(
                registeredUser.id(),
                registeredUser.email(),
                registeredUser.displayName(),
                encoder.encode("secret123"),
                registeredUser.role(),
                registeredUser.isActive()
        ));

        AtomicBoolean tokenIssued = new AtomicBoolean(false);
        var useCase = new LoginUseCase(users, encoder, user -> {
            tokenIssued.set(true);
            return "token";
        });

        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("user@test.com", "wrong-password")));
        assertFalse(tokenIssued.get());
    }

    @Test
    @SpecificationRef(value = "AUTH-03", level = TestLevel.UNIT, feature = "authentication.feature")
    void rejects_login_when_user_is_inactive_even_with_correct_password() {
        var users = new InMemoryUserRepository();
        var inactiveUser = DomainTestDataFactory.inactiveUser("inactive@test.com", "Inactive User", UserRole.USER);
        users.put(new com.aperdigon.ticketing_backend.domain.user.User(
                inactiveUser.id(),
                inactiveUser.email(),
                inactiveUser.displayName(),
                encoder.encode("secret123"),
                inactiveUser.role(),
                inactiveUser.isActive()
        ));

        AtomicBoolean tokenIssued = new AtomicBoolean(false);
        var useCase = new LoginUseCase(users, encoder, user -> {
            tokenIssued.set(true);
            return "token";
        });

        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("inactive@test.com", "secret123")));
        assertFalse(tokenIssued.get());
    }

    @Test
    void rejects_login_when_password_is_null() {
        var users = new InMemoryUserRepository();
        var registeredUser = DomainTestDataFactory.activeUser("user@test.com", "User", UserRole.USER);
        users.put(new com.aperdigon.ticketing_backend.domain.user.User(
                registeredUser.id(),
                registeredUser.email(),
                registeredUser.displayName(),
                encoder.encode("secret123"),
                registeredUser.role(),
                registeredUser.isActive()
        ));

        var useCase = new LoginUseCase(users, encoder, user -> "token");

        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("user@test.com", null)));
    }
}
