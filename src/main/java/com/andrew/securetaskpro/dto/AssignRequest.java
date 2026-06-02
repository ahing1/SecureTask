package com.andrew.securetaskpro.dto;

import jakarta.validation.constraints.NotNull;

public class AssignRequest {
    @NotNull(message = "Assignee id cant be null.")
    private Long assigneeId;

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
