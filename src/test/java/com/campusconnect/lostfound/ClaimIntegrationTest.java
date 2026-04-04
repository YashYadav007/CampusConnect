package com.campusconnect.lostfound;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusconnect.lostfound.repository.ClaimRequestRepository;
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
class ClaimIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private LostFoundPostRepository lostFoundPostRepository;
  @Autowired private ClaimRequestRepository claimRequestRepository;

  @Test
  void claim_create_view_approve_reject_rules_work_end_to_end() throws Exception {
    String ownerEmail = "lf-owner@example.com";
    String claimer1Email = "claimer1@example.com";
    String claimer2Email = "claimer2@example.com";

    register("Owner", ownerEmail);
    register("Claimer One", claimer1Email);
    register("Claimer Two", claimer2Email);

    String ownerToken = login(ownerEmail);
    String claimer1Token = login(claimer1Email);
    String claimer2Token = login(claimer2Email);

    long foundPostId = createPost(ownerToken, "FOUND", "Found wallet near library");
    long lostPostId = createPost(ownerToken, "LOST", "Lost bottle near canteen");

    // Public can still read posts
    mockMvc.perform(get("/api/lost-found/" + foundPostId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Claims endpoint is not public
    mockMvc.perform(get("/api/lost-found/" + foundPostId + "/claims"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    // Viewing claims for LOST posts should fail (even for owner)
    mockMvc.perform(get("/api/lost-found/" + lostPostId + "/claims")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Cannot claim LOST post
    mockMvc.perform(post("/api/lost-found/" + lostPostId + "/claim")
            .header("Authorization", "Bearer " + claimer1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"This is mine, I can describe it clearly.\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Cannot claim own post
    mockMvc.perform(post("/api/lost-found/" + foundPostId + "/claim")
            .header("Authorization", "Bearer " + ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"I posted this, should not be able to claim.\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Create claim 1 (message trimmed)
    String claim1Resp = mockMvc.perform(post("/api/lost-found/" + foundPostId + "/claim")
            .header("Authorization", "Bearer " + claimer1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"   This wallet has my student ID card inside.   \"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andExpect(jsonPath("$.data.postId").value((int) foundPostId))
        .andExpect(jsonPath("$.data.message").value("This wallet has my student ID card inside."))
        .andReturn().getResponse().getContentAsString();

    long claim1Id = objectMapper.readTree(claim1Resp).path("data").path("id").asLong();

    // Duplicate pending claim rejected
    mockMvc.perform(post("/api/lost-found/" + foundPostId + "/claim")
            .header("Authorization", "Bearer " + claimer1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"This wallet is mine, please approve.\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Create claim 2
    String claim2Resp = mockMvc.perform(post("/api/lost-found/" + foundPostId + "/claim")
            .header("Authorization", "Bearer " + claimer2Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"I can tell you the exact brand and contents of this wallet.\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    long claim2Id = objectMapper.readTree(claim2Resp).path("data").path("id").asLong();

    Assertions.assertEquals(2, claimRequestRepository.count());

    // Non-owner cannot view claims
    mockMvc.perform(get("/api/lost-found/" + foundPostId + "/claims")
            .header("Authorization", "Bearer " + claimer1Token))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    // Owner can view claims (latest first)
    mockMvc.perform(get("/api/lost-found/" + foundPostId + "/claims")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].id").value((int) claim2Id))
        .andExpect(jsonPath("$.data[1].id").value((int) claim1Id));

    // Non-owner cannot approve
    mockMvc.perform(post("/api/claims/" + claim1Id + "/approve")
            .header("Authorization", "Bearer " + claimer1Token))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    // Owner approves claim1 -> post RESOLVED, other pending REJECTED
    mockMvc.perform(post("/api/claims/" + claim1Id + "/approve")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("APPROVED"));

    // Post is resolved
    mockMvc.perform(get("/api/lost-found/" + foundPostId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("RESOLVED"));

    // Other claim is rejected
    mockMvc.perform(get("/api/lost-found/" + foundPostId + "/claims")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value((int) claim2Id))
        .andExpect(jsonPath("$.data[0].status").value("REJECTED"));

    // Cannot approve already handled claim
    mockMvc.perform(post("/api/claims/" + claim1Id + "/approve")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Cannot create claim on resolved post
    mockMvc.perform(post("/api/lost-found/" + foundPostId + "/claim")
            .header("Authorization", "Bearer " + claimer1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"Trying again after resolved.\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // Rejection flow keeps post OPEN
    long foundPost2Id = createPost(ownerToken, "FOUND", "Found earbuds in lab");
    String claim3Resp = mockMvc.perform(post("/api/lost-found/" + foundPost2Id + "/claim")
            .header("Authorization", "Bearer " + claimer1Token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"message\":\"These earbuds are mine, I can tell the case color.\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    long claim3Id = objectMapper.readTree(claim3Resp).path("data").path("id").asLong();

    // Non-owner cannot reject
    mockMvc.perform(post("/api/claims/" + claim3Id + "/reject")
            .header("Authorization", "Bearer " + claimer1Token))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc.perform(post("/api/claims/" + claim3Id + "/reject")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("REJECTED"));

    mockMvc.perform(get("/api/lost-found/" + foundPost2Id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("OPEN"));

    // Cannot reject already handled claim
    mockMvc.perform(post("/api/claims/" + claim3Id + "/reject")
            .header("Authorization", "Bearer " + ownerToken))
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

  private long createPost(String token, String type, String title) throws Exception {
    String resp = mockMvc.perform(post("/api/lost-found")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                  \"type\": \"%s\",
                  \"title\": \"%s\",
                  \"description\": \"Some details about the item\",
                  \"location\": \"Central Library\",
                  \"dateOfIncident\": \"2026-04-02\"
                }
                """, type, title)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(resp).path("data").path("id").asLong();
  }
}
