package com.aperdigon.ticketing_backend.application.admin.users.update_active;

import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public final class UpdateUserActiveUseCase {

    private final UserRepository userRepository;

    public UpdateUserActiveUseCase(UserRepository userRepository) {
        this.userRepository = Guard.notNull(userRepository, "userRepository");
    }

    public User execute(UpdateUserActiveCommand command) {
        Guard.notNull(command, "command");

        var existing = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        var updated = new User(
                existing.id(),
                existing.email(),
                existing.displayName(),
                existing.passwordHash(),
                existing.role(),
                command.isActive()
        );

        return userRepository.save(updated);
    }
}
