package com.campusconnect.config;

import com.campusconnect.enums.ClaimStatus;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
import com.campusconnect.enums.RoleName;
import com.campusconnect.enums.VoteType;
import com.campusconnect.lostfound.entity.ClaimRequest;
import com.campusconnect.lostfound.entity.LostFoundPost;
import com.campusconnect.lostfound.repository.ClaimRequestRepository;
import com.campusconnect.lostfound.repository.LostFoundPostRepository;
import com.campusconnect.qa.entity.Answer;
import com.campusconnect.qa.entity.Question;
import com.campusconnect.qa.entity.Tag;
import com.campusconnect.qa.entity.Vote;
import com.campusconnect.qa.repository.AnswerRepository;
import com.campusconnect.qa.repository.QuestionRepository;
import com.campusconnect.qa.repository.TagRepository;
import com.campusconnect.qa.repository.VoteRepository;
import com.campusconnect.user.entity.Role;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.repository.RoleRepository;
import com.campusconnect.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo data seeding for local demos.
 *
 * <p>Enabled by property via {@code app.demo.seed=true}. It is idempotent: if users already exist, only the demo
 * admin account is ensured and bulk demo content is skipped.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
@ConditionalOnProperty(prefix = "app.demo", name = "seed", havingValue = "true", matchIfMissing = false)
public class DemoDataSeeder implements CommandLineRunner {

  private static final String DEMO_PASSWORD = "password123";

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  private final TagRepository tagRepository;
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;
  private final VoteRepository voteRepository;

  private final LostFoundPostRepository lostFoundPostRepository;
  private final ClaimRequestRepository claimRequestRepository;

  @Override
  public void run(String... args) {
    seed();
  }

  @Transactional
  void seed() {
    Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
        .orElseThrow(() -> new IllegalStateException("ROLE_STUDENT not seeded"));
    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
        .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not seeded"));

    ensureDemoAdmin(adminRole);

    if (userRepository.count() > 0) {
      long nonAdminUsers = userRepository.count() - 1;
      if (nonAdminUsers > 0) {
        log.info("Demo seeding skipped (users already exist); ensured admin account only");
        return;
      }
    }

    if (userRepository.existsByEmail("aarav@demo.com")) {
      log.info("Demo seeding skipped (demo data already present)");
      return;
    }

    // Users (demo accounts)
    User a = createUser(studentRole, "Aarav Sharma", "aarav@demo.com", "B.Tech CSE", 3);
    User b = createUser(studentRole, "Diya Patel", "diya@demo.com", "BCA", 2);
    User c = createUser(studentRole, "Kabir Singh", "kabir@demo.com", "B.Tech IT", 4);

    // Tags
    Tag tJava = tag("java");
    Tag tSpring = tag("spring");
    Tag tJwt = tag("jwt");
    Tag tSql = tag("mysql");
    Tag tReact = tag("react");

    // Questions (4–6)
    Question q1 = question(a,
        "JWT token not validating in Spring Security (Spring Boot 3)",
        "I implemented JWT auth but requests keep returning 401. What are the common mistakes in filter setup?",
        Set.of(tJava, tSpring, tJwt));
    Question q2 = question(b,
        "Best way to model ManyToMany tags in JPA?",
        "Should I use a join entity or a join table for question-tags? Need clean architecture guidance.",
        Set.of(tJava, tSpring));
    Question q3 = question(c,
        "MySQL connection fails locally: Communications link failure",
        "Spring Boot app starts but DB connection fails. Using MySQL on localhost. What should I verify?",
        Set.of(tSql, tSpring));
    Question q4 = question(a,
        "React + Vite: how to proxy API requests to Spring Boot?",
        "I want to avoid CORS during development. How should I configure the Vite dev server proxy?",
        Set.of(tReact));
    Question q5 = question(b,
        "Pagination design: Page vs List response for frontend",
        "My endpoints return List with page/size params. Should I return Page metadata too? Pros/cons?",
        Set.of(tSpring, tReact));

    // Answers (6–10) with accepted/votes
    Answer a11 = answer(b, q1, "Check that your JwtAuthenticationFilter runs before UsernamePasswordAuthenticationFilter and that you set SecurityContext on valid tokens.");
    Answer a12 = answer(c, q1, "Common issue: token prefix missing (Bearer) or username extraction mismatch. Also ensure your JWT secret is 32+ chars for HS256.");
    // accept a11 (question owner = a)
    acceptAnswer(a11);

    Answer a21 = answer(a, q2, "Join table is fine for simple tags. Use @ManyToMany with a join table; move to a join entity only if you need extra fields.");
    Answer a22 = answer(c, q2, "If you need tag usage count or audit info, a join entity is cleaner. Otherwise ManyToMany is OK.");

    Answer a31 = answer(b, q3, "Verify MySQL is running, URL/port correct, and credentials. Also ensure the database exists and user has permissions.");
    Answer a32 = answer(a, q3, "If using Docker, confirm port mapping. Also check MySQL timeouts and driver version.");
    acceptAnswer(a31); // question owner = c, but for demo seeding we directly set the flag

    Answer a41 = answer(c, q4, "In vite.config.js, add server.proxy for /api to http://localhost:8080. Then use relative baseURL in axios.");

    Answer a51 = answer(a, q5, "For scalability, return Page metadata (totalElements, totalPages). If you keep List, frontend can only infer hasNext.");

    // Votes (simulate activity)
    upvote(c, a11); // b +10
    upvote(a, a11); // b +10
    upvote(a, a12); // c +10
    downvote(b, a12); // c -2
    upvote(b, a21); // a +10
    upvote(c, a31); // b +10
    upvote(a, a31); // b +10
    upvote(a, a41); // c +10

    // Reputation (compute from seeded actions)
    // Accepted answers: +15
    // Upvote: +10, Downvote: -2
    applyReputation(b, (15 + 20) /*a11 accepted + 2 upvotes*/ + (15 + 20) /*a31 accepted + 2 upvotes*/);
    applyReputation(c, 10 /*a12*/ - 2 /*downvote*/ + 10 /*a41*/); // c
    applyReputation(a, 10 /*a21*/); // a

    userRepository.saveAll(List.of(a, b, c));

    // Lost & Found posts (3–5) + claims (pending + approved)
    LostFoundPost lf1 = lostFound(a, PostType.FOUND, "Found black wallet near Central Library",
        "Looks like it may contain ID cards. If it’s yours, describe the contents.",
        "Central Library", LocalDate.now().minusDays(1),
        "https://picsum.photos/seed/campusconnect-wallet/1200/800",
        ItemStatus.OPEN);

    LostFoundPost lf2 = lostFound(b, PostType.LOST, "Lost AirPods case near cafeteria",
        "White case with minor scratch. Please contact if found.",
        "Main Cafeteria", LocalDate.now().minusDays(2),
        "https://picsum.photos/seed/campusconnect-airpods/1200/800",
        ItemStatus.OPEN);

    LostFoundPost lf3 = lostFound(a, PostType.FOUND, "Found keys with blue keychain",
        "Two keys and one tag. Found near Admin block.",
        "Admin Block", LocalDate.now().minusDays(3),
        "https://picsum.photos/seed/campusconnect-keys/1200/800",
        ItemStatus.RESOLVED);

    LostFoundPost lf4 = lostFound(c, PostType.FOUND, "Found smartwatch near sports complex",
        "Black strap, small scratch on screen. Found near the entrance.",
        "Sports Complex", LocalDate.now().minusDays(1),
        "https://picsum.photos/seed/campusconnect-watch/1200/800",
        ItemStatus.OPEN);

    // Extra FOUND + OPEN post owned by Aarav with a pending claim (so owner can demo claim management immediately).
    LostFoundPost lf5 = lostFound(a, PostType.FOUND, "Found student ID card near auditorium",
        "College ID card found near the auditorium entrance. Claim with correct details.",
        "Auditorium", LocalDate.now().minusDays(1),
        "https://picsum.photos/seed/campusconnect-idcard/1200/800",
        ItemStatus.OPEN);

    // Claims for lf5 (pending)
    ClaimRequest cr51 = claim(lf5, b, "This is my ID card. It has my name and roll number printed; I can confirm the exact details.", ClaimStatus.PENDING);

    // Claims for lf3 (approved + rejected)
    ClaimRequest cr31 = claim(lf3, b, "These keys are mine. The blue keychain has my hostel room number tag attached.", ClaimStatus.APPROVED);
    ClaimRequest cr32 = claim(lf3, c, "I believe these keys are mine; I can describe the tag and key shapes.", ClaimStatus.REJECTED);

    // Claims for lf4 (pending)
    ClaimRequest cr41 = claim(lf4, a, "This smartwatch is mine. It has a tiny scratch on the top-right and the watch face is set to dark mode.", ClaimStatus.PENDING);

    claimRequestRepository.saveAll(List.of(cr51, cr31, cr32, cr41));

    log.info(
        "Demo seeding completed. Demo logins: admin@campusconnect.com, aarav@demo.com, diya@demo.com, kabir@demo.com / {}",
        DEMO_PASSWORD
    );
  }

  private void ensureDemoAdmin(Role adminRole) {
    userRepository.findByEmail("admin@campusconnect.com").orElseGet(() -> {
      User admin = User.builder()
          .fullName("Campus Admin")
          .email("admin@campusconnect.com")
          .password(passwordEncoder.encode(DEMO_PASSWORD))
          .course("Administration")
          .yearOfStudy(0)
          .reputationPoints(0)
          .isActive(true)
          .roles(Set.of(adminRole))
          .build();
      log.info("Demo admin ensured: admin@campusconnect.com / {}", DEMO_PASSWORD);
      return userRepository.save(admin);
    });
  }

  private User createUser(Role role, String fullName, String email, String course, int year) {
    User u = User.builder()
        .fullName(fullName)
        .email(email)
        .password(passwordEncoder.encode(DEMO_PASSWORD))
        .course(course)
        .yearOfStudy(year)
        .reputationPoints(0)
        .isActive(true)
        .roles(Set.of(role))
        .build();
    return userRepository.save(u);
  }

  private Tag tag(String raw) {
    String name = raw.trim().toLowerCase();
    return tagRepository.findByName(name).orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
  }

  private Question question(User author, String title, String description, Set<Tag> tags) {
    Question q = Question.builder()
        .title(title)
        .description(description)
        .user(author)
        .tags(tags)
        .build();
    return questionRepository.save(q);
  }

  private Answer answer(User author, Question question, String content) {
    Answer a = Answer.builder()
        .content(content)
        .question(question)
        .user(author)
        .isAccepted(false)
        .build();
    return answerRepository.save(a);
  }

  private void acceptAnswer(Answer a) {
    a.setIsAccepted(true);
    answerRepository.save(a);
  }

  private void upvote(User voter, Answer answer) {
    if (answer.getUser().getId().equals(voter.getId())) return;
    Vote v = Vote.builder().answer(answer).user(voter).voteType(VoteType.UPVOTE).build();
    voteRepository.save(v);
  }

  private void downvote(User voter, Answer answer) {
    if (answer.getUser().getId().equals(voter.getId())) return;
    Vote v = Vote.builder().answer(answer).user(voter).voteType(VoteType.DOWNVOTE).build();
    voteRepository.save(v);
  }

  private void applyReputation(User user, int delta) {
    user.setReputationPoints(user.getReputationPoints() + delta);
  }

  private LostFoundPost lostFound(
      User owner,
      PostType type,
      String title,
      String description,
      String location,
      LocalDate date,
      String imageUrl,
      ItemStatus status
  ) {
    LostFoundPost p = LostFoundPost.builder()
        .user(owner)
        .type(type)
        .title(title)
        .description(description)
        .imageUrl(imageUrl)
        .location(location)
        .dateOfIncident(date)
        .status(status)
        .build();
    return lostFoundPostRepository.save(p);
  }

  private ClaimRequest claim(LostFoundPost post, User claimer, String message, ClaimStatus status) {
    ClaimRequest c = ClaimRequest.builder()
        .post(post)
        .claimer(claimer)
        .message(message)
        .status(status)
        .build();
    return c;
  }
}
