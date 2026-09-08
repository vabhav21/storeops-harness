package com.cognizant.storeops.programmes.routes;

import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.model.ProjectStatus;
import com.cognizant.storeops.programmes.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createProject_givenValidBody_thenReturns201WithStatusActive() throws Exception {
        Project created = new Project("Winter Refit", "store-1", "region-1");
        created.setStatus(ProjectStatus.ACTIVE);
        when(projectService.createProject(anyString(), anyString(), anyString())).thenReturn(created);

        String body = """
                {"name":"Winter Refit","storeId":"store-1","regionId":"region-1"}
                """;

        mockMvc.perform(post("/api/programmes")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                // Business rule check, not just status code (failure mode #3):
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.storeId").value("store-1"));
    }

    @Test
    void getProject_givenMissingStoreOrRegionParam_thenReturns400ValidationError() throws Exception {
        mockMvc.perform(get("/api/programmes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void applyTemplate_givenValidRequest_thenReturns202Accepted() throws Exception {
        Project active = new Project("New Store Setup", "store-6", "region-3");
        active.setStatus(ProjectStatus.ACTIVE);
        UUID id = UUID.randomUUID();
        when(projectService.applyTemplate(any(UUID.class), any())).thenReturn(active);

        String body = """
                {"templateName":"Standard Planogram","tasks":[
                  {"title":"Reset endcap 3","department":"Grocery","priority":"HIGH","category":"PLANOGRAM"}
                ]}
                """;

        mockMvc.perform(post("/api/programmes/" + id + "/templates")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.storeId").value("store-6"));
    }
}
