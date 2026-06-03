package com.andrew.securetaskpro.controller;

import com.andrew.securetaskpro.dto.TaskRequest;
import com.andrew.securetaskpro.dto.TaskResponse;
import com.andrew.securetaskpro.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private TaskResponse sampleResponse() {
        return new TaskResponse(1L, "New title", "desc", "TODO", "HIGH",
                null, 10L, null, 1L, LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void shouldCreateTaskAndReturn201WhenAuthenticated() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("New title");
        request.setStatus("TODO");
        request.setPriority("HIGH");

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    @WithMockUser
    void shouldReturn200AndTasksWhenAuthenticated() throws Exception {
        when(taskService.getTasks(any())).thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTaskById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound());
    }
}
