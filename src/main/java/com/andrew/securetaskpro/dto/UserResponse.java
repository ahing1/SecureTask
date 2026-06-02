package com.andrew.securetaskpro.dto;

public class UserResponse {

    private Long id;
    private String username;
    private String role;
    private Long organizationId;

    public UserResponse(Long id, String username, String role, Long organizationId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.organizationId = organizationId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
