package com.aperdigon.ticketing_backend.infrastructure.seed;

import java.util.UUID;

import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalSeedRunner implements CommandLineRunner {

    // UUIDs fijos para que puedas generar tokens con sub conocido
    public static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID AGENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID CATEGORY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserSpringDataRepository userRepo;
    private final CategorySpringDataRepository categoryRepo;

    public LocalSeedRunner(UserSpringDataRepository userRepo, CategorySpringDataRepository categoryRepo) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public void run(String... args) {
        if (categoryRepo.findById(CATEGORY_ID).isEmpty()) {
            categoryRepo.save(new CategoryJpaEntity(CATEGORY_ID, "General", true));
        }

        if (userRepo.findById(USER_ID).isEmpty()) {
            userRepo.save(new UserJpaEntity(USER_ID, "user@local.test", "Local User", PASSWORD_ENCODER.encode("local-user-password"), UserRole.USER, true));
        }

        if (userRepo.findById(AGENT_ID).isEmpty()) {
            userRepo.save(new UserJpaEntity(AGENT_ID, "agent@local.test", "Local Agent", PASSWORD_ENCODER.encode("local-agent-password"), UserRole.AGENT, true));
        }

        System.out.println("Local seed ready:");
        System.out.println("  USER_ID    = " + USER_ID);
        System.out.println("  AGENT_ID   = " + AGENT_ID);
        System.out.println("  CATEGORY_ID= " + CATEGORY_ID);
    }
}
