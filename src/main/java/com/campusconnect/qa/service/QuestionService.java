package com.campusconnect.qa.service;

import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.qa.dto.AnswerResponse;
import com.campusconnect.qa.dto.AuthorResponse;
import com.campusconnect.qa.dto.CreateQuestionRequest;
import com.campusconnect.qa.dto.QuestionDetailResponse;
import com.campusconnect.qa.dto.QuestionResponse;
import com.campusconnect.qa.entity.Question;
import com.campusconnect.qa.entity.Tag;
import com.campusconnect.qa.repository.AnswerRepository;
import com.campusconnect.qa.repository.QuestionRepository;
import com.campusconnect.qa.repository.TagRepository;
import com.campusconnect.qa.repository.QuestionAnswerCount;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {

  private static final int MAX_PAGE_SIZE = 50;
  private static final int MIN_TAG_LEN = 2;
  private static final int MAX_TAG_LEN = 30;

  private final QuestionRepository questionRepository;
  private final TagRepository tagRepository;
  private final AnswerRepository answerRepository;
  private final UserService userService;
  private final VoteService voteService;

  @Transactional
  public QuestionResponse createQuestion(CreateQuestionRequest req) {
    User currentUser = userService.getCurrentUserEntity();

    String title = req.getTitle() == null ? null : req.getTitle().trim();
    if (title == null || title.isBlank()) {
      throw new BadRequestException("Title cannot be blank");
    }

    String description = normalizeOptionalText(req.getDescription());

    Set<Tag> tags = resolveTags(req.getTags());

    Question question = Question.builder()
        .title(title)
        .description(description)
        .user(currentUser)
        .tags(tags)
        .build();

    Question saved = questionRepository.save(question);
    log.info("Question created: id={} user={} title={}", saved.getId(), currentUser.getEmail(), safeTitle(saved.getTitle()));

    return toQuestionResponse(saved, 0L);
  }

  @Transactional(readOnly = true)
  public List<QuestionResponse> getAllQuestions(int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size));
    List<Question> questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable).getContent();
    Map<Long, Long> answerCountByQuestionId = loadAnswerCounts(questions);

    return questions.stream()
        .map(q -> toQuestionResponse(q, answerCountByQuestionId.getOrDefault(q.getId(), 0L)))
        .toList();
  }

  @Transactional(readOnly = true)
  public QuestionDetailResponse getQuestionById(Long id) {
    Question question = questionRepository.findWithUserAndTagsById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

    // Fetch answers + authors in one query to avoid N+1.
    var answerEntities = answerRepository.findByQuestionIdWithUserOrderByCreatedAtAsc(id);
    var voteSummaries = voteService.loadVoteSummaries(answerEntities.stream().map(a -> a.getId()).toList());

    List<AnswerResponse> answers = answerEntities.stream()
        .map(a -> {
          AnswerVoteSummary s = voteSummaries.getOrDefault(a.getId(), AnswerVoteSummary.zero());
          return AnswerResponse.builder()
              .id(a.getId())
              .content(a.getContent())
              .user(toAuthor(a.getUser()))
              .createdAt(a.getCreatedAt())
              .isAccepted(a.getIsAccepted())
              .upvoteCount(s.upvotes())
              .downvoteCount(s.downvotes())
              .score(s.score())
              .build();
        })
        .toList();

    return QuestionDetailResponse.builder()
        .id(question.getId())
        .title(question.getTitle())
        .description(question.getDescription())
        .user(toAuthor(question.getUser()))
        .tags(question.getTags().stream().map(Tag::getName).sorted().toList())
        .createdAt(question.getCreatedAt())
        .answers(answers)
        .build();
  }

  @Transactional(readOnly = true)
  public List<QuestionResponse> searchQuestions(String keyword, int page, int size) {
    String k = keyword == null ? "" : keyword.trim();
    if (k.isBlank()) {
      throw new BadRequestException("keyword is required");
    }

    Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));

    List<Question> questions = questionRepository.searchByTitleContainingIgnoreCase(k, pageable).getContent();
    Map<Long, Long> answerCountByQuestionId = loadAnswerCounts(questions);

    return questions.stream()
        .map(q -> toQuestionResponse(q, answerCountByQuestionId.getOrDefault(q.getId(), 0L)))
        .toList();
  }

  private Set<Tag> resolveTags(List<String> rawTags) {
    if (rawTags == null || rawTags.isEmpty()) {
      return Collections.emptySet();
    }

    Set<String> normalized = new LinkedHashSet<>();
    for (String t : rawTags) {
      String n = normalizeTag(t);
      if (n != null) {
        normalized.add(n);
      }
    }

    if (normalized.isEmpty()) {
      return Collections.emptySet();
    }

    return normalized.stream().map(this::getOrCreateTag).collect(Collectors.toSet());
  }

  private Tag getOrCreateTag(String name) {
    return tagRepository.findByName(name).orElseGet(() -> {
      try {
        return tagRepository.save(Tag.builder().name(name).build());
      } catch (DataIntegrityViolationException ex) {
        // Handles tag creation race conditions.
        return tagRepository.findByName(name)
            .orElseThrow(() -> ex);
      }
    });
  }

  private String normalizeTag(String raw) {
    if (raw == null) {
      return null;
    }

    // Requirement: trim, lowercase, remove duplicates (handled by Set), ignore empty.
    String n = raw.trim().toLowerCase(Locale.ROOT);
    if (n.isBlank()) {
      return null;
    }
    if (n.length() < MIN_TAG_LEN || n.length() > MAX_TAG_LEN) {
      throw new BadRequestException("Tag length must be between " + MIN_TAG_LEN + " and " + MAX_TAG_LEN);
    }
    return n;
  }

  private int clampPageSize(int size) {
    if (size <= 0) {
      return 20;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  private QuestionResponse toQuestionResponse(Question q, Long answerCount) {
    return QuestionResponse.builder()
        .id(q.getId())
        .title(q.getTitle())
        .description(q.getDescription())
        .user(toAuthor(q.getUser()))
        .tags(q.getTags().stream().map(Tag::getName).sorted().toList())
        .answerCount(answerCount)
        .createdAt(q.getCreatedAt())
        .build();
  }

  private AuthorResponse toAuthor(User u) {
    return AuthorResponse.builder().id(u.getId()).fullName(u.getFullName()).build();
  }

  private String safeTitle(String title) {
    if (title == null) {
      return "";
    }
    String t = title.replaceAll("\\s+", " ").trim();
    return t.length() <= 80 ? t : t.substring(0, 80);
  }

  private String normalizeOptionalText(String raw) {
    if (raw == null) {
      return null;
    }
    String t = raw.trim();
    return t.isBlank() ? null : t;
  }

  private Map<Long, Long> loadAnswerCounts(List<Question> questions) {
    if (questions == null || questions.isEmpty()) {
      return Collections.emptyMap();
    }

    List<Long> ids = questions.stream().map(Question::getId).toList();
    List<QuestionAnswerCount> counts = answerRepository.countAnswersByQuestionIds(ids);

    Map<Long, Long> map = new HashMap<>();
    for (QuestionAnswerCount c : counts) {
      if (c.getQuestionId() != null && c.getAnswerCount() != null) {
        map.put(c.getQuestionId(), c.getAnswerCount());
      }
    }
    return map;
  }
}
