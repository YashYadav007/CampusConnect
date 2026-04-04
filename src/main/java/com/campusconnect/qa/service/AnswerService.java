package com.campusconnect.qa.service;

import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.common.exception.UnauthorizedException;
import com.campusconnect.qa.dto.AnswerResponse;
import com.campusconnect.qa.dto.AuthorResponse;
import com.campusconnect.qa.dto.CreateAnswerRequest;
import com.campusconnect.qa.entity.Answer;
import com.campusconnect.qa.entity.Question;
import com.campusconnect.qa.repository.AnswerRepository;
import com.campusconnect.qa.repository.QuestionRepository;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerService {

  private static final int REP_ACCEPT = 15;

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;
  private final UserService userService;
  private final VoteService voteService;

  @Transactional
  public AnswerResponse addAnswer(Long questionId, CreateAnswerRequest req) {
    if (req.getContent() == null || req.getContent().trim().isBlank()) {
      throw new BadRequestException("Content cannot be blank");
    }

    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

    User currentUser = userService.getCurrentUserEntity();

    Answer answer = Answer.builder()
        .content(req.getContent().trim())
        .question(question)
        .user(currentUser)
        .isAccepted(false)
        .build();

    Answer saved = answerRepository.save(answer);
    log.info("Answer added: id={} questionId={} user={}", saved.getId(), questionId, currentUser.getEmail());

    return toResponse(saved, AnswerVoteSummary.zero());
  }

  @Transactional(readOnly = true)
  public List<AnswerResponse> getAnswersByQuestionId(Long questionId) {
    // Validate question exists for cleaner API behaviour.
    if (!questionRepository.existsById(questionId)) {
      throw new ResourceNotFoundException("Question not found");
    }

    // Fetch answers + authors in one query to avoid N+1.
    List<Answer> answers = answerRepository.findByQuestionIdWithUserOrderByCreatedAtAsc(questionId);
    Map<Long, AnswerVoteSummary> summaries = voteService.loadVoteSummaries(answers.stream().map(Answer::getId).toList());

    return answers.stream()
        .map(a -> toResponse(a, summaries.getOrDefault(a.getId(), AnswerVoteSummary.zero())))
        .toList();
  }

  @Transactional
  public AnswerResponse acceptAnswer(Long answerId) {
    User currentUser = userService.getCurrentUserEntity();

    Answer target = answerRepository.findByIdWithQuestionAndUsers(answerId)
        .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

    Long questionOwnerId = target.getQuestion().getUser().getId();
    if (!questionOwnerId.equals(currentUser.getId())) {
      throw new UnauthorizedException("Only the question owner can accept an answer");
    }

    // Serialize accept/unaccept operations per question to avoid reputation double-apply under concurrency.
    questionRepository.findByIdForUpdate(target.getQuestion().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

    if (Boolean.TRUE.equals(target.getIsAccepted())) {
      // Idempotent
      Map<Long, AnswerVoteSummary> summaries = voteService.loadVoteSummaries(List.of(target.getId()));
      return toResponse(target, summaries.getOrDefault(target.getId(), AnswerVoteSummary.zero()));
    }

    Answer previouslyAccepted = answerRepository.findAcceptedByQuestionId(target.getQuestion().getId()).orElse(null);
    if (previouslyAccepted != null && !previouslyAccepted.getId().equals(target.getId())) {
      previouslyAccepted.setIsAccepted(false);
      applyReputationDelta(previouslyAccepted.getUser(), -REP_ACCEPT);
    }

    target.setIsAccepted(true);
    applyReputationDelta(target.getUser(), REP_ACCEPT);

    Answer saved = answerRepository.save(target);
    log.info("Answer accepted: answerId={} questionId={} byUser={}", saved.getId(), saved.getQuestion().getId(),
        currentUser.getEmail());

    Map<Long, AnswerVoteSummary> summaries = voteService.loadVoteSummaries(List.of(saved.getId()));
    return toResponse(saved, summaries.getOrDefault(saved.getId(), AnswerVoteSummary.zero()));
  }

  private void applyReputationDelta(User user, int delta) {
    user.setReputationPoints(user.getReputationPoints() + delta);
  }

  private AnswerResponse toResponse(Answer a, AnswerVoteSummary summary) {
    return AnswerResponse.builder()
        .id(a.getId())
        .content(a.getContent())
        .user(AuthorResponse.builder().id(a.getUser().getId()).fullName(a.getUser().getFullName()).build())
        .createdAt(a.getCreatedAt())
        .isAccepted(a.getIsAccepted())
        .upvoteCount(summary.upvotes())
        .downvoteCount(summary.downvotes())
        .score(summary.score())
        .build();
  }
}
