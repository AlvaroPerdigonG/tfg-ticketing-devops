package com.aperdigon.ticketing_backend.unit.admin;

import com.aperdigon.ticketing_backend.application.admin.users.list.ListAdminUsersUseCase;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ListAdminUsersUseCaseTest {

    @Test
    void lists_users_sorted_by_display_name_including_inactive_users() {
        var repository = new InMemoryUserRepository();
        var zed = DomainTestDataFactory.activeUser("zed@test.com", "Zed", UserRole.AGENT);
        var amy = DomainTestDataFactory.inactiveUser("amy@test.com", "Amy", UserRole.USER);
        repository.put(zed);
        repository.put(amy);
        var useCase = new ListAdminUsersUseCase(repository);

        var result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals(amy.id(), result.get(0).id());
        assertEquals(zed.id(), result.get(1).id());
    }
}
