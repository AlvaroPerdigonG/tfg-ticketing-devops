package com.aperdigon.ticketing_backend.infrastructure.persistence.adapter;

import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper.UserMapper;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {

    private final UserSpringDataRepository springRepo;

    public JpaUserRepository(UserSpringDataRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springRepo.findById(id.value()).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepo.findByEmailIgnoreCase(email).map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var savedEntity = springRepo.save(UserMapper.toJpa(user));
        return UserMapper.toDomain(savedEntity);
    }
}
