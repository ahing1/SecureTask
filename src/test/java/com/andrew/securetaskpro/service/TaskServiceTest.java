package com.andrew.securetaskpro.service;

import com.andrew.securetaskpro.dto.TaskRequest;
import com.andrew.securetaskpro.dto.TaskResponse;
import com.andrew.securetaskpro.model.Task;
import com.andrew.securetaskpro.model.User;
import com.andrew.securetaskpro.repository.TaskRepository;
import com.andrew.securetaskpro.repository.UserRepository;
import com.andrew.securetaskpro.security.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskServiceTest {

    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private SecurityHelper securityHelper;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        taskRepository = mock(TaskRepository.class);
        securityHelper = mock(SecurityHelper.class);
        taskService = new TaskService(userRepository, securityHelper, taskRepository);
    }

    /** Builds a current user with the given id, for stubbing securityHelper.getCurrentUser(). */
    private User currentUser(Long id) {
        User user = new User("andrew", "hashed", "ADMIN", 1L);
        user.setId(id);
        return user;
    }

    @Test
    void createTaskSavesTaskWithCorrectCreatorIdAndOrganizationId() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Write tests");
        request.setStatus("TODO");
        request.setPriority("HIGH");
        request.setAssigneeId(null); // no assignee -> skip the assignee checks

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(securityHelper.getCurrentUser()).thenReturn(currentUser(10L));
        // save() returns whatever it was given, so toResponse() has a real Task
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        taskService.createTask(request);

        // capture the Task actually handed to the repository
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task saved = taskCaptor.getValue();

        assertEquals(10L, saved.getCreatorId());
        assertEquals(1L, saved.getOrganizationId());
        assertEquals("Write tests", saved.getTitle());
    }

    @Test
    void createTaskThrowsWhenAssigneeDoesNotBelongToOrganization() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Write tests");
        request.setStatus("TODO");
        request.setPriority("HIGH");
        request.setAssigneeId(99L);

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(securityHelper.getCurrentUser()).thenReturn(currentUser(10L));

        // the assignee exists, but belongs to a DIFFERENT organization (2L, not 1L)
        User assignee = new User("bob", "hashed", "ADMIN", 2L);
        when(userRepository.findById(99L)).thenReturn(Optional.of(assignee));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        // a rejected request must not persist a task
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTasksReturnsOnlyTasksScopedToCurrentOrganization() {
        Pageable pageable = PageRequest.of(0, 10);
        Task task = new Task("Write tests", null, "TODO", "HIGH", null, 10L, null, 1L);
        Page<Task> page = new PageImpl<>(List.of(task));

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByOrganizationId(1L, pageable)).thenReturn(page);

        Page<TaskResponse> result = taskService.getTasks(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getOrganizationId());

        // the query must be scoped to the current org's id, never some other org
        verify(taskRepository).findByOrganizationId(eq(1L), any(Pageable.class));
    }

    @Test
    void getTaskByIdReturnsTaskWhenFoundInOrganization() {
        Task task = new Task("Write tests", null, "TODO", "HIGH", null, 10L, null, 1L);

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(5L);

        assertEquals("Write tests", response.getTitle());
        assertEquals(1L, response.getOrganizationId());
    }

    @Test
    void getTaskByIdThrows404WhenTaskNotFound() {
        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTaskById(5L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateStatusThrows403WhenCurrentUserIsNotAssignee() {
        // task is assigned to user 99, but the current user is 10
        Task task = new Task("Write tests", null, "TODO", "HIGH", null, 10L, 99L, 1L);

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.of(task));
        when(securityHelper.getCurrentUser()).thenReturn(currentUser(10L));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateStatus(5L, "DONE"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        // a forbidden update must not change or persist the task
        assertEquals("TODO", task.getStatus());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateStatusUpdatesWhenCurrentUserIsAssignee() {
        // task is assigned to user 10, and the current user is also 10
        Task task = new Task("Write tests", null, "TODO", "HIGH", null, 10L, 10L, 1L);

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.of(task));
        when(securityHelper.getCurrentUser()).thenReturn(currentUser(10L));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateStatus(5L, "DONE");

        assertEquals("DONE", response.getStatus());
        assertEquals("DONE", task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTaskThrows404WhenTaskNotFound() {
        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.deleteTask(5L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        // nothing should be deleted if the task was never found
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void deleteTaskCallsDeleteByIdWhenTaskFound() {
        Task task = new Task("Write tests", null, "TODO", "HIGH", null, 10L, null, 1L);

        when(securityHelper.getCurrentOrganizationId()).thenReturn(1L);
        when(taskRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(5L);

        verify(taskRepository).deleteById(5L);
    }
}