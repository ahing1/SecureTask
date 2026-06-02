package com.andrew.securetaskpro.service;

import com.andrew.securetaskpro.dto.AssignRequest;
import com.andrew.securetaskpro.dto.TaskRequest;
import com.andrew.securetaskpro.dto.TaskResponse;
import com.andrew.securetaskpro.model.Task;
import com.andrew.securetaskpro.model.User;
import com.andrew.securetaskpro.repository.TaskRepository;
import com.andrew.securetaskpro.repository.UserRepository;
import com.andrew.securetaskpro.security.SecurityHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;
    private final TaskRepository taskRepository;

    public TaskService(UserRepository userRepository, SecurityHelper securityHelper, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.securityHelper = securityHelper;
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(TaskRequest request) {

        Long orgId = securityHelper.getCurrentOrganizationId();
        Long assigneeId = request.getAssigneeId();
        Long creatorId = securityHelper.getCurrentUser().getId();

        if(assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));

            if(!assignee.getOrganizationId().equals(orgId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignee is not in your organization");
            }
        }

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority(),
                request.getDueDate(),
                creatorId,
                assigneeId,
                orgId
        );

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public Page<TaskResponse> getTasks(Pageable pageable) {

        Long orgId = securityHelper.getCurrentOrganizationId();

        return taskRepository.findByOrganizationId(orgId, pageable)
                .map(this::toResponse);
    }

    public TaskResponse getTaskById(Long id) {

        Long orgId = securityHelper.getCurrentOrganizationId();

        Task task = taskRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        return toResponse(task);
    }

    public TaskResponse updateStatus(Long id, String status) {

        Long orgId = securityHelper.getCurrentOrganizationId();

        Task task = taskRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Long curId = securityHelper.getCurrentUser().getId();
        if(!curId.equals(task.getAssigneeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user is not assignee");
        }

        task.setStatus(status);
        Task saved = taskRepository.save(task);
        return toResponse(saved);

    }

    public TaskResponse assignTask(Long id, AssignRequest request) {
        Long orgId = securityHelper.getCurrentOrganizationId();

        Task task = taskRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Long newAssigneeId = request.getAssigneeId();
        User assignee = userRepository.findById(newAssigneeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));

        if(!assignee.getOrganizationId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignee is not in your organization");
        }

        task.setAssigneeId(newAssigneeId);
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public void deleteTask(Long id) {
        Long orgId = securityHelper.getCurrentOrganizationId();
        Task task = taskRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        taskRepository.deleteById(id);

    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatorId(),
                task.getAssigneeId(),
                task.getOrganizationId(),
                task.getCreatedAt()
        );
    }

}
