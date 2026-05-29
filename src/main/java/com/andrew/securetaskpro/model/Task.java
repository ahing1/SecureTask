package com.andrew.securetaskpro.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Task() {}

    public Task(String title, String description, String status,
                String priority, LocalDate dueDate,
                Long creatorId, Long assigneeId, Long organizationId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.creatorId = creatorId;
        this.assigneeId = assigneeId;
        this.organizationId = organizationId;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public Long getCreatorId() { return creatorId; }
    public Long getAssigneeId() { return assigneeId; }
    public Long getOrganizationId() { return organizationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

}
