package com.andrew.securetaskpro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    public User() {}

    public User(String username, String password, String role, Long organizationId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.organizationId = organizationId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
