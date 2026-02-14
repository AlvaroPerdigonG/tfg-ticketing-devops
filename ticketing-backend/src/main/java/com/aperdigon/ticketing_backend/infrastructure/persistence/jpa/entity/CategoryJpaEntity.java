package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    protected CategoryJpaEntity() {}

    public CategoryJpaEntity(UUID id, String name, boolean isActive) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return isActive; }
}
