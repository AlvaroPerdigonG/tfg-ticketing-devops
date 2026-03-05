package com.aperdigon.ticketing_backend.application.ports;

import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;

import java.util.Optional;
import java.util.List;

public interface UserRepository {
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User save(User user);
}
