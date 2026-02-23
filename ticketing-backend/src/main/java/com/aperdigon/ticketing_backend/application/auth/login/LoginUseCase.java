package com.aperdigon.ticketing_backend.application.auth.login;

import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
    }

    public LoginResult execute(LoginCommand command) {
        String email = normalizeEmail(command.email());
        String password = command.password() == null ? "" : command.password();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return new LoginResult(tokenIssuer.issue(user));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
