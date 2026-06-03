package com.andrew.securetaskpro.controller;

import com.andrew.securetaskpro.dto.AssignRequest;
import com.andrew.securetaskpro.dto.TaskRequest;
import com.andrew.securetaskpro.dto.TaskResponse;
import com.andrew.securetaskpro.dto.UpdateStatusRequest;
import com.andrew.securetaskpro.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                taskService.createTask(request)
        );
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@Valid @RequestBody UpdateStatusRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return ResponseEntity.ok(taskService.assignTask(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }
}
