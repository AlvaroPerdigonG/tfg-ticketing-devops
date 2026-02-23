package com.aperdigon.ticketing_backend.infrastructure.seed;

import java.util.UUID;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalSeedRunner implements CommandLineRunner {

    public static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID AGENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID CATEGORY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private final UserSpringDataRepository userRepo;
    private final CategorySpringDataRepository categoryRepo;
    private final PasswordEncoder passwordEncoder;

    public LocalSeedRunner(UserSpringDataRepository userRepo, CategorySpringDataRepository categoryRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (categoryRepo.findById(CATEGORY_ID).isEmpty()) {
            categoryRepo.save(new CategoryJpaEntity(CATEGORY_ID, "General", true));
        }

        if (userRepo.findById(USER_ID).isEmpty()) {
            userRepo.save(new UserJpaEntity(
                    USER_ID,
                    "user@local.test",
                    "Local User",
                    passwordEncoder.encode("password"),
                    UserRole.USER,
                    true
            ));
        }

        if (userRepo.findById(AGENT_ID).isEmpty()) {
            userRepo.save(new UserJpaEntity(
                    AGENT_ID,
                    "agent@local.test",
                    "Local Agent",
                    passwordEncoder.encode("password"),
                    UserRole.AGENT,
                    true
            ));
        }
    }
}
