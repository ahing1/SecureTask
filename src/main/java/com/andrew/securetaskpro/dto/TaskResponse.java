package com.andrew.securetaskpro.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private Long creatorId;
    private Long assigneeId;
    private Long organizationId;
    private LocalDateTime createdAt;

    public TaskResponse(Long id, String title, String description, String status, String priority, LocalDate dueDate, Long creatorId, Long assigneeId, Long organizationId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.creatorId = creatorId;
        this.assigneeId = assigneeId;
        this.organizationId = organizationId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
