package com.circuitbreaker.gbpservice.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OpenAPI Spec Tests - gbp-service")
class OpenApiSpecTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs should return valid OpenAPI JSON")
    void apiDocs_ShouldReturnValidJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").value(startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("GBP Service API"))
                .andExpect(jsonPath("$.info.version").exists());
    }

    @Test
    @DisplayName("OpenAPI spec should contain expected endpoints")
    void apiDocs_ShouldContainExpectedEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/time']").exists())
                .andExpect(jsonPath("$.paths['/api/time'].get").exists());
    }

    @Test
    @DisplayName("OpenAPI spec should contain schema definitions")
    void apiDocs_ShouldContainSchemaDefinitions() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.TimeResponse").exists());
    }
}
