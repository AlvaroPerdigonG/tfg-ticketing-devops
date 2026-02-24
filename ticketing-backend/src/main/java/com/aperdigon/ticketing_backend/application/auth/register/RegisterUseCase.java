package com.aperdigon.ticketing_backend.application.auth.register;

import com.aperdigon.ticketing_backend.application.auth.login.TokenIssuer;
import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class RegisterUseCase {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public RegisterUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
    }

    public RegisterResult execute(RegisterCommand command) {
        String email = normalizeEmail(command.email());
        String displayName = normalizeDisplayName(command.displayName());
        String password = command.password() == null ? "" : command.password();
        String confirmPassword = command.confirmPassword() == null ? "" : command.confirmPassword();

        if (!password.equals(confirmPassword)) {
            throw new InvalidArgumentException("Passwords do not match");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidArgumentException("Password must have at least 8 characters, including uppercase, lowercase, number and symbol");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new InvalidArgumentException("Registration failed");
        }

        User createdUser = new User(
                UserId.of(UUID.randomUUID()),
                email,
                displayName,
                passwordEncoder.encode(password),
                UserRole.USER,
                true
        );

        User savedUser = userRepository.save(createdUser);
        return new RegisterResult(tokenIssuer.issue(savedUser));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    private String normalizeDisplayName(String displayName) {
        if (displayName == null) {
            return "";
        }
        return displayName.trim();
    }
}
