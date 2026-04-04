package com.campusconnect.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void register_login_me_and_swagger_work() throws Exception {
    // Register
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"fullName\": \"Test User\",
                  \"email\": \"test@example.com\",
                  \"password\": \"password123\",
                  \"course\": \"CSE\",
                  \"yearOfStudy\": 3
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"));

    // /me without token -> 401
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    // Login
    String loginBody = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"email\": \"test@example.com\",
                  \"password\": \"password123\"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").isString())
        .andExpect(jsonPath("$.data.user.id").isNumber())
        .andExpect(jsonPath("$.data.user.fullName").value("Test User"))
        .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.user.roles[0]").value("ROLE_STUDENT"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode json = objectMapper.readTree(loginBody);
    String token = json.path("data").path("token").asText();

    // /me with token -> 200
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"));

    // OpenAPI should be publicly accessible
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk());

    // Swagger UI should be publicly accessible
    mockMvc.perform(get("/swagger-ui/index.html"))
        .andExpect(status().isOk());
  }
}
