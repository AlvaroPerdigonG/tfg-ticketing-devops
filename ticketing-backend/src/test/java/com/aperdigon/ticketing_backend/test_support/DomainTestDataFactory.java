package com.aperdigon.ticketing_backend.test_support;

import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;

import java.time.Clock;
import java.util.UUID;

public final class DomainTestDataFactory {

    private static final String SECURE_HASH = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";

    private DomainTestDataFactory() {
    }

    public static User activeUser(String email, String displayName, UserRole role) {
        return new User(UserId.of(UUID.randomUUID()), email, displayName, SECURE_HASH, role, true);
    }

    public static User inactiveUser(String email, String displayName, UserRole role) {
        return new User(UserId.of(UUID.randomUUID()), email, displayName, SECURE_HASH, role, false);
    }

    public static Category activeCategory(String name) {
        return new Category(CategoryId.of(UUID.randomUUID()), name, true);
    }

    public static Ticket openTicket(String title, String description, Category category, User creator, Clock clock) {
        return Ticket.openNew(title, description, category, creator, clock);
    }

    public static Ticket openTicket(String title, String description, Category category, User creator, TicketPriority priority, Clock clock) {
        return Ticket.openNew(title, description, category, creator, priority, clock);
    }
}
