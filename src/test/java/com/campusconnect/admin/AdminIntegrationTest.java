package com.campusconnect.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusconnect.enums.RoleName;
import com.campusconnect.lostfound.repository.ClaimRequestRepository;
import com.campusconnect.qa.repository.AnswerRepository;
import com.campusconnect.qa.repository.QuestionRepository;
import com.campusconnect.qa.repository.VoteRepository;
import com.campusconnect.user.entity.Role;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.repository.RoleRepository;
import com.campusconnect.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private RoleRepository roleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private QuestionRepository questionRepository;
  @Autowired private AnswerRepository answerRepository;
  @Autowired private VoteRepository voteRepository;
  @Autowired private ClaimRequestRepository claimRequestRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void admin_routes_and_moderation_work_end_to_end() throws Exception {
    register("Owner", "owner@example.com");
    register("Answerer", "answerer@example.com");
    register("Claimer", "claimer@example.com");
    createAdmin("admin@campusconnect.com");

    String ownerToken = login("owner@example.com");
    String answererToken = login("answerer@example.com");
    String claimerToken = login("claimer@example.com");
    String adminToken = login("admin@campusconnect.com");

    long question1Id = createQuestion(ownerToken, "How do I secure Spring Boot endpoints?");
    long answer1Id = addAnswer(answererToken, question1Id, "Use Spring Security with JWT filters and a stateless session.");
    vote(ownerToken, answer1Id, "UPVOTE");

    long question2Id = createQuestion(ownerToken, "How should I design admin moderation cleanup?");
    long answer2Id = addAnswer(answererToken, question2Id, "Delete child rows first or use explicit cascade rules.");

    long postId = createPost(ownerToken, "FOUND", "Found calculator in lab");
    createClaim(claimerToken, postId, "This calculator is mine, I can confirm the model and stickers on it.");

    mockMvc.perform(get("/api/admin/stats")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalUsers").value(4))
        .andExpect(jsonPath("$.data.activeUsers").value(4))
        .andExpect(jsonPath("$.data.totalQuestions").value(2))
        .andExpect(jsonPath("$.data.totalAnswers").value(2))
        .andExpect(jsonPath("$.data.totalLostFoundPosts").value(1))
        .andExpect(jsonPath("$.data.openLostFoundPosts").value(1))
        .andExpect(jsonPath("$.data.totalClaims").value(1))
        .andExpect(jsonPath("$.data.pendingClaims").value(1));

    mockMvc.perform(get("/api/admin/stats")
            .header("Authorization", "Bearer " + ownerToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false));

    mockMvc.perform(get("/api/admin/users")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.data[?(@.email=='admin@campusconnect.com')]").exists());

    mockMvc.perform(get("/api/admin/questions")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2));

    mockMvc.perform(get("/api/admin/questions/" + question1Id)
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value((int) question1Id))
        .andExpect(jsonPath("$.data.answers.length()").value(1));

    mockMvc.perform(get("/api/admin/questions/" + question1Id + "/answers")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value((int) answer1Id))
        .andExpect(jsonPath("$.data[0].score").value(1));

    mockMvc.perform(delete("/api/admin/answers/" + answer1Id)
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    Assertions.assertFalse(answerRepository.findById(answer1Id).isPresent());
    Assertions.assertEquals(0, answerRepository.countByQuestionId(question1Id));
    Assertions.assertEquals(0, voteRepository.count());

    mockMvc.perform(delete("/api/admin/questions/" + question2Id)
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    Assertions.assertFalse(questionRepository.findById(question2Id).isPresent());
    Assertions.assertFalse(answerRepository.findById(answer2Id).isPresent());

    mockMvc.perform(get("/api/admin/lost-found")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));

    mockMvc.perform(get("/api/admin/claims")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].postId").value((int) postId));

    mockMvc.perform(delete("/api/admin/lost-found/" + postId)
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    mockMvc.perform(get("/api/lost-found/" + postId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));

    Assertions.assertEquals(0, claimRequestRepository.count());

    Long ownerId = userRepository.findByEmail("owner@example.com").orElseThrow().getId();

    mockMvc.perform(patch("/api/admin/users/" + ownerId + "/deactivate")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isActive").value(false));

    Assertions.assertFalse(userRepository.findById(ownerId).orElseThrow().getIsActive());

    mockMvc.perform(patch("/api/admin/users/" + ownerId + "/activate")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isActive").value(true));

    Assertions.assertTrue(userRepository.findById(ownerId).orElseThrow().getIsActive());
  }

  private void createAdmin(String email) {
    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
    User admin = User.builder()
        .fullName("Campus Admin")
        .email(email)
        .password(passwordEncoder.encode("password123"))
        .course("Administration")
        .yearOfStudy(0)
        .reputationPoints(0)
        .isActive(true)
        .roles(Set.of(adminRole))
        .build();
    userRepository.save(admin);
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

  private long createQuestion(String token, String title) throws Exception {
    String resp = mockMvc.perform(post("/api/questions")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                  \"title\": \"%s\",
                  \"description\": \"Admin moderation test question\",
                  \"tags\": [\"java\", \"spring\"]
                }
                """, title)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(resp).path("data").path("id").asLong();
  }

  private long addAnswer(String token, long questionId, String content) throws Exception {
    String resp = mockMvc.perform(post("/api/questions/" + questionId + "/answers")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("{\"content\":\"%s\"}", content)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(resp).path("data").path("id").asLong();
  }

  private void vote(String token, long answerId, String voteType) throws Exception {
    mockMvc.perform(post("/api/answers/" + answerId + "/vote")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("{\"voteType\":\"%s\"}", voteType)))
        .andExpect(status().isOk());
  }

  private long createPost(String token, String type, String title) throws Exception {
    String resp = mockMvc.perform(post("/api/lost-found")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                  \"type\": \"%s\",
                  \"title\": \"%s\",
                  \"description\": \"Admin moderation test post\",
                  \"location\": \"Innovation Lab\",
                  \"dateOfIncident\": \"2026-04-02\"
                }
                """, type, title)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(resp).path("data").path("id").asLong();
  }

  private void createClaim(String token, long postId, String message) throws Exception {
    mockMvc.perform(post("/api/lost-found/" + postId + "/claim")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("{\"message\":\"%s\"}", message)))
        .andExpect(status().isOk());
  }
}
