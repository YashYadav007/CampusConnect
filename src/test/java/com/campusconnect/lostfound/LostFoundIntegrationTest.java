package com.campusconnect.lostfound;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusconnect.lostfound.repository.LostFoundPostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
class LostFoundIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private LostFoundPostRepository lostFoundPostRepository;

  @Test
  void lost_found_create_list_get_filter_and_validation_work() throws Exception {
    // Public can list
    mockMvc.perform(get("/api/lost-found"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Unauthenticated create rejected
    mockMvc.perform(post("/api/lost-found")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"type\": \"LOST\",
                  \"title\": \"Black wallet near library\",
                  \"description\": \"May contain student ID cards\",
                  \"imageUrl\": \"https://example.com/wallet.png\",
                  \"location\": \"Central Library\",
                  \"dateOfIncident\": \"2026-04-02\"
                }
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    register("LF User", "lf@example.com");
    String token = login("lf@example.com");

    // Create LOST with blank description -> stored as null
    String createdLost = mockMvc.perform(post("/api/lost-found")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"type\": \"LOST\",
                  \"title\": \"Black wallet near library\",
                  \"description\": \"   \",
                  \"imageUrl\": \"https://example.com/wallet.png\",
                  \"location\": \"Central Library\",
                  \"dateOfIncident\": \"2026-04-02\"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.type").value("LOST"))
        .andExpect(jsonPath("$.data.status").value("OPEN"))
        .andExpect(jsonPath("$.data.description").value(org.hamcrest.Matchers.nullValue()))
        .andReturn()
        .getResponse()
        .getContentAsString();

    long lostId = objectMapper.readTree(createdLost).path("data").path("id").asLong();

    // Create FOUND
    String createdFound = mockMvc.perform(post("/api/lost-found")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"type\": \"FOUND\",
                  \"title\": \"Found keys near cafeteria\",
                  \"description\": \"Keychain with 3 keys\",
                  \"location\": \"Main Cafeteria\",
                  \"dateOfIncident\": \"2026-04-02\"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.type").value("FOUND"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    long foundId = objectMapper.readTree(createdFound).path("data").path("id").asLong();

    Assertions.assertEquals(2L, lostFoundPostRepository.count());

    // Public list
    mockMvc.perform(get("/api/lost-found"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2));

    // Pagination (size=1)
    mockMvc.perform(get("/api/lost-found").param("page", "0").param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));

    // Filter by type
    mockMvc.perform(get("/api/lost-found").param("type", "LOST"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].type").value("LOST"));

    // Filter by status
    mockMvc.perform(get("/api/lost-found").param("status", "OPEN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2));

    // Filter by both
    mockMvc.perform(get("/api/lost-found").param("type", "FOUND").param("status", "OPEN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].id").value((int) foundId));

    // Get single (public)
    mockMvc.perform(get("/api/lost-found/" + lostId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value((int) lostId))
        .andExpect(jsonPath("$.data.user.fullName").value("LF User"));

    // Not found -> 404
    mockMvc.perform(get("/api/lost-found/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));

    // Invalid enum in query param -> 400
    mockMvc.perform(get("/api/lost-found").param("type", "INVALID"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Invalid enum in body -> 400
    mockMvc.perform(post("/api/lost-found")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"type\": \"INVALID\",
                  \"title\": \"Some title\",
                  \"location\": \"Somewhere\",
                  \"dateOfIncident\": \"2026-04-02\"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Missing required fields -> 400
    mockMvc.perform(post("/api/lost-found")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"type\": \"LOST\",
                  \"title\": \"\",
                  \"location\": \"\",
                  \"dateOfIncident\": null
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  private void register(String fullName, String email) throws Exception {
    String body = String.format(
        "{\"fullName\":\"%s\",\"email\":\"%s\",\"password\":\"password123\",\"course\":\"CSE\",\"yearOfStudy\":3}",
        fullName,
        email
    );

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk());
  }

  private String login(String email) throws Exception {
    String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

    String resp = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode json = objectMapper.readTree(resp);
    return json.path("data").path("token").asText();
  }
}
