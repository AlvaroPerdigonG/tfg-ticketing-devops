package com.aperdigon.ticketing_backend.unit.auth;

import com.aperdigon.ticketing_backend.application.auth.profile.GetMyProfileUseCase;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetMyProfileUseCaseTest {

    @Test
    void returns_profile_for_current_user() {
        var users = new InMemoryUserRepository();
        var user = DomainTestDataFactory.activeUser("user@test.com", "Test User", UserRole.AGENT);
        users.put(user);

        var useCase = new GetMyProfileUseCase(users);
        var result = useCase.execute(new CurrentUser(user.id(), user.role()));

        assertEquals(user.id().value(), result.userId());
        assertEquals("user@test.com", result.email());
        assertEquals("Test User", result.displayName());
        assertEquals(UserRole.AGENT, result.role());
        assertEquals(List.of("AGENT"), result.roles());
    }

    @Test
    void throws_not_found_when_current_user_does_not_exist() {
        var useCase = new GetMyProfileUseCase(new InMemoryUserRepository());

        assertThrows(NotFoundException.class, () -> useCase.execute(
                new CurrentUser(
                        com.aperdigon.ticketing_backend.domain.user.UserId.of(UUID.randomUUID()),
                        UserRole.USER
                )
        ));
    }
}
