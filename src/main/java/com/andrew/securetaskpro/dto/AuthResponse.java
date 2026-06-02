package com.andrew.securetaskpro.dto;

public class AuthResponse {

    private String token;
    private String username;
    private String role;
    private Long organizationId;

    public AuthResponse(String token, String username, String role, Long organizationId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.organizationId = organizationId;
    }

    public String getToken() {
        return token;
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
