package com.andrew.securetaskpro.dto;

import java.time.LocalDateTime;

public class OrganizationResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public OrganizationResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
