package com.aperdigon.ticketing_backend.unit.admin;

import com.aperdigon.ticketing_backend.application.admin.users.update_active.UpdateUserActiveCommand;
import com.aperdigon.ticketing_backend.application.admin.users.update_active.UpdateUserActiveUseCase;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UpdateUserActiveUseCaseTest {

    @Test
    void updates_active_flag_without_changing_identity_or_role() {
        var repository = new InMemoryUserRepository();
        var user = DomainTestDataFactory.activeUser("agent@test.com", "Agent", UserRole.AGENT);
        repository.put(user);
        var useCase = new UpdateUserActiveUseCase(repository);

        var updated = useCase.execute(new UpdateUserActiveCommand(user.id(), false));

        assertEquals(user.id(), updated.id());
        assertEquals(user.email(), updated.email());
        assertEquals(user.displayName(), updated.displayName());
        assertEquals(user.role(), updated.role());
        assertFalse(updated.isActive());
    }

    @Test
    void rejects_missing_user() {
        var useCase = new UpdateUserActiveUseCase(new InMemoryUserRepository());

        assertThrows(NotFoundException.class, () -> useCase.execute(new UpdateUserActiveCommand(
                UserId.of(UUID.randomUUID()),
                false
        )));
    }
}
