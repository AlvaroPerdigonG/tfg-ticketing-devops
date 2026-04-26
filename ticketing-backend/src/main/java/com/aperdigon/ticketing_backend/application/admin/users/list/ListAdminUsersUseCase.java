package com.aperdigon.ticketing_backend.application.admin.users.list;

import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class ListAdminUsersUseCase {

    private final UserRepository userRepository;

    public ListAdminUsersUseCase(UserRepository userRepository) {
        this.userRepository = Guard.notNull(userRepository, "userRepository");
    }

    public List<User> execute() {
        return userRepository.findAll();
    }
}
