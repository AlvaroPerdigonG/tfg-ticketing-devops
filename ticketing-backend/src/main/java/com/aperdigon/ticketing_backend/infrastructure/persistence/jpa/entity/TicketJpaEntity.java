package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity;

import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class TicketJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TicketStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private UserJpaEntity createdBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private UserJpaEntity assignedTo;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<CommentJpaEntity> comments = new ArrayList<>();

    protected TicketJpaEntity() {}

    public TicketJpaEntity(
            UUID id,
            String title,
            String description,
            TicketStatus status,
            Instant createdAt,
            Instant updatedAt,
            UserJpaEntity createdBy,
            CategoryJpaEntity category,
            UserJpaEntity assignedTo
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.category = category;
        this.assignedTo = assignedTo;
    }

    // getters/setters mínimos para el mapper
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UserJpaEntity getCreatedBy() { return createdBy; }
    public CategoryJpaEntity getCategory() { return category; }
    public UserJpaEntity getAssignedTo() { return assignedTo; }
    public List<CommentJpaEntity> getComments() { return comments; }

    public void setStatus(TicketStatus status) { this.status = status; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setAssignedTo(UserJpaEntity assignedTo) { this.assignedTo = assignedTo; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(CategoryJpaEntity category) { this.category = category; }
    public void setCreatedBy(UserJpaEntity createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void setComments(List<CommentJpaEntity> comments) { this.comments = comments; }
}
