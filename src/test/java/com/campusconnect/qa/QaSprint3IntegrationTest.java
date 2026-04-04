package com.campusconnect.qa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusconnect.user.repository.UserRepository;
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
class QaSprint3IntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;

  @Test
  void voting_acceptance_and_reputation_work_end_to_end() throws Exception {
    String ownerEmail = "owner@example.com";
    String author1Email = "author1@example.com";
    String author2Email = "author2@example.com";
    String voterEmail = "voter@example.com";

    register("Owner", ownerEmail);
    register("Author One", author1Email);
    register("Author Two", author2Email);
    register("Voter", voterEmail);

    String ownerToken = login(ownerEmail);
    String author1Token = login(author1Email);
    String author2Token = login(author2Email);
    String voterToken = login(voterEmail);

    long questionId = createQuestion(ownerToken);

    long answer1Id = addAnswer(author1Token, questionId, "JWT is a signed token used for auth");
    long answer2Id = addAnswer(author2Token, questionId, "JWT is a compact, URL-safe token");

    // voter upvotes answer1 -> +10
    vote(voterToken, answer1Id, "UPVOTE")
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.upvoteCount").value(1))
        .andExpect(jsonPath("$.data.downvoteCount").value(0))
        .andExpect(jsonPath("$.data.score").value(1));
    Assertions.assertEquals(10, rep(author1Email));

    // same upvote toggles off -> -10
    vote(voterToken, answer1Id, "UPVOTE")
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.upvoteCount").value(0))
        .andExpect(jsonPath("$.data.score").value(0));
    Assertions.assertEquals(0, rep(author1Email));

    // downvote -> -2
    vote(voterToken, answer1Id, "DOWNVOTE")
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.downvoteCount").value(1))
        .andExpect(jsonPath("$.data.score").value(-1));
    Assertions.assertEquals(-2, rep(author1Email));

    // change to upvote -> reverse -2 then +10 => +10
    vote(voterToken, answer1Id, "UPVOTE")
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.upvoteCount").value(1))
        .andExpect(jsonPath("$.data.downvoteCount").value(0))
        .andExpect(jsonPath("$.data.score").value(1));
    Assertions.assertEquals(10, rep(author1Email));

    // author cannot vote own answer
    vote(author1Token, answer1Id, "UPVOTE")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));

    // owner accepts answer1 -> +15 (total 25)
    accept(ownerToken, answer1Id)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isAccepted").value(true));
    Assertions.assertEquals(25, rep(author1Email));

    // accepting already accepted answer should be idempotent
    accept(ownerToken, answer1Id)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isAccepted").value(true));
    Assertions.assertEquals(25, rep(author1Email));

    // non-owner cannot accept
    accept(voterToken, answer2Id)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));

    // accept second answer -> unaccept first: author1 -15, author2 +15
    accept(ownerToken, answer2Id)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isAccepted").value(true));

    Assertions.assertEquals(10, rep(author1Email));
    Assertions.assertEquals(15, rep(author2Email));

    // question detail should show only one accepted answer
    mockMvc.perform(get("/api/questions/" + questionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.answers.length()").value(2))
        .andExpect(jsonPath("$.data.answers[0].isAccepted").value(false))
        .andExpect(jsonPath("$.data.answers[1].isAccepted").value(true));

    // answers endpoint includes vote counts
    mockMvc.perform(get("/api/questions/" + questionId + "/answers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].upvoteCount").value(1))
        .andExpect(jsonPath("$.data[0].score").value(1));
  }

  private int rep(String email) {
    return userRepository.findByEmail(email).orElseThrow().getReputationPoints();
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

  private long createQuestion(String token) throws Exception {
    String resp = mockMvc.perform(post("/api/questions")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  \"title\": \"Explain JWT in Spring Security\",
                  \"description\": \"What is it used for?\",
                  \"tags\": [\"java\", \"spring\"]
                }
                """))
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

  private org.springframework.test.web.servlet.ResultActions vote(String token, long answerId, String type) throws Exception {
    return mockMvc.perform(post("/api/answers/" + answerId + "/vote")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(String.format("{\"voteType\":\"%s\"}", type)));
  }

  private org.springframework.test.web.servlet.ResultActions accept(String token, long answerId) throws Exception {
    return mockMvc.perform(post("/api/answers/" + answerId + "/accept")
            .header("Authorization", "Bearer " + token));
  }
}
