package com.aperdigon.ticketing_backend.application.auth.profile;

import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetMyProfileUseCase {

    private final UserRepository userRepository;

    public GetMyProfileUseCase(UserRepository userRepository) {
        this.userRepository = Guard.notNull(userRepository, "userRepository");
    }

    public GetMyProfileResult execute(CurrentUser actor) {
        Guard.notNull(actor, "actor");

        var user = userRepository.findById(actor.id())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new GetMyProfileResult(
                user.id().value(),
                user.email(),
                user.displayName(),
                user.role(),
                List.of(user.role().name())
        );
    }
}
