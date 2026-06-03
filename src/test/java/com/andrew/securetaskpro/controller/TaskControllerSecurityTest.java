package com.andrew.securetaskpro.controller;

import com.andrew.securetaskpro.dto.TaskRequest;
import com.andrew.securetaskpro.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void shouldReturn401WhenCreatingTaskWithoutToken() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("New title");
        request.setStatus("TODO");
        request.setPriority("HIGH");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteTaskWhenAdmin() throws Exception {
        mockMvc.perform(delete("/tasks/5").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenDeletingTaskAsMember() throws Exception {
        mockMvc.perform(delete("/tasks/5").with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());

        verify(taskService, never()).deleteTask(anyLong());
    }
}
