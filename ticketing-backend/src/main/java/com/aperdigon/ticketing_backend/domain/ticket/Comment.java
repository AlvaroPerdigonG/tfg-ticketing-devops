package com.aperdigon.ticketing_backend.domain.ticket;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;

import java.time.Instant;

public final class Comment {
    private final CommentId id;
    private final String content;
    private final UserId authorId;
    private final Instant createdAt;

    public Comment(CommentId id, String content, User author, Instant createdAt) {
        this.id = Guard.notNull(id, "id");
        this.content = Guard.notBlank(content, "content");
        this.authorId = Guard.notNull(Guard.notNull(author, "author").id(), "authorId");
        this.createdAt = Guard.notNull(createdAt, "createdAt");
    }

    public CommentId id() { return id; }
    public String content() { return content; }
    public UserId authorId() { return authorId; }
    public Instant createdAt() { return createdAt; }
}
