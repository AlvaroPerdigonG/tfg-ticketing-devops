package com.aperdigon.ticketing_backend.unit.auth;

import com.aperdigon.ticketing_backend.application.auth.login.LoginCommand;
import com.aperdigon.ticketing_backend.application.auth.login.LoginUseCase;
import com.aperdigon.ticketing_backend.application.auth.login.TokenIssuer;
import com.aperdigon.ticketing_backend.application.shared.exception.UnauthorizedException;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoginUseCaseTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void returns_token_when_credentials_are_valid() {
        var repo = new InMemoryUserRepository();
        User user = new User(UserId.of(UUID.randomUUID()), "user@test.com", "User", encoder.encode("secret"), UserRole.USER, true);
        repo.put(user);

        TokenIssuer tokenIssuer = u -> "jwt-token";
        var useCase = new LoginUseCase(repo, encoder, tokenIssuer);

        var result = useCase.execute(new LoginCommand("user@test.com", "secret"));

        assertEquals("jwt-token", result.accessToken());
    }

    @Test
    void throws_when_email_does_not_exist() {
        var useCase = new LoginUseCase(new InMemoryUserRepository(), encoder, u -> "token");
        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("missing@test.com", "secret")));
    }

    @Test
    void throws_when_password_is_invalid() {
        var repo = new InMemoryUserRepository();
        repo.put(new User(UserId.of(UUID.randomUUID()), "user@test.com", "User", encoder.encode("secret"), UserRole.USER, true));

        var useCase = new LoginUseCase(repo, encoder, u -> "token");
        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("user@test.com", "wrong")));
    }

    @Test
    void throws_when_user_is_inactive() {
        var repo = new InMemoryUserRepository();
        repo.put(new User(UserId.of(UUID.randomUUID()), "user@test.com", "User", encoder.encode("secret"), UserRole.USER, false));

        var useCase = new LoginUseCase(repo, encoder, u -> "token");
        assertThrows(UnauthorizedException.class, () -> useCase.execute(new LoginCommand("user@test.com", "secret")));
    }
}
