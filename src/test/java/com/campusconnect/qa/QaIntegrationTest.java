package com.campusconnect.qa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusconnect.qa.repository.TagRepository;
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
class QaIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TagRepository tagRepository;

  @Test
  void qa_flow_public_reads_and_auth_writes_work() throws Exception {
    // Public read should work
    mockMvc.perform(get("/api/questions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Creating a question without token should be rejected
    mockMvc.perform(post("/api/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"title\": \"What is JWT?\",
                  \"description\": \"Need an explanation\",
                  \"tags\": [\"java\", \"spring\"]
                }
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    // Register + login for token
    register("qa@example.com");
    String token = loginAndGetToken("qa@example.com");

    // Create question (auth)
    String created = mockMvc.perform(post("/api/questions")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"title\": \"What is JWT?\",
                  \"description\": \"Need an explanation\",
                  \"tags\": [\"java\", \"spring\"]
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").isNumber())
        .andExpect(jsonPath("$.data.user.fullName").value("QA User"))
        .andExpect(jsonPath("$.data.tags[0]").isString())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long questionId = objectMapper.readTree(created).path("data").path("id").asLong();

    // Tags persisted and reused
    org.junit.jupiter.api.Assertions.assertEquals(2L, tagRepository.count());

    // Add answer (auth)
    mockMvc.perform(post("/api/questions/" + questionId + "/answers")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { \"content\": \"JWT is a signed token used for stateless auth\" }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.user.fullName").value("QA User"));

    // List includes answerCount
    mockMvc.perform(get("/api/questions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].answerCount").value(1));

    // Public: view question detail with answers
    mockMvc.perform(get("/api/questions/" + questionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.answers[0].content").value("JWT is a signed token used for stateless auth"));

    // Public: search
    mockMvc.perform(get("/api/questions/search").param("keyword", "jwt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Create second question with existing tag -> tag count should remain 2
    mockMvc.perform(post("/api/questions")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"title\": \"Spring Security basics\",
                  \"description\": \"How does the filter chain work?\",
                  \"tags\": [\"java\"]
                }
                """))
        .andExpect(status().isOk());

    org.junit.jupiter.api.Assertions.assertEquals(2L, tagRepository.count());
  }

  private void register(String email) throws Exception {
    String body = String.format(
        "{\"fullName\":\"QA User\",\"email\":\"%s\",\"password\":\"password123\",\"course\":\"CSE\",\"yearOfStudy\":3}",
        email
    );
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk());
  }

  private String loginAndGetToken(String email) throws Exception {
    String login = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);
    String body = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(login))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode json = objectMapper.readTree(body);
    return json.path("data").path("token").asText();
  }
}
