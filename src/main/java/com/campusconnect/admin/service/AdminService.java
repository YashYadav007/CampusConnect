package com.campusconnect.admin.service;

import com.campusconnect.admin.dto.AdminAnswerResponse;
import com.campusconnect.admin.dto.AdminClaimResponse;
import com.campusconnect.admin.dto.AdminDashboardResponse;
import com.campusconnect.admin.dto.AdminLostFoundResponse;
import com.campusconnect.admin.dto.AdminQuestionDetailResponse;
import com.campusconnect.admin.dto.AdminQuestionResponse;
import com.campusconnect.admin.dto.AdminUserResponse;
import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.enums.ClaimStatus;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.lostfound.entity.ClaimRequest;
import com.campusconnect.lostfound.entity.LostFoundPost;
import com.campusconnect.lostfound.repository.ClaimRequestRepository;
import com.campusconnect.lostfound.repository.LostFoundPostRepository;
import com.campusconnect.qa.entity.Answer;
import com.campusconnect.qa.entity.Question;
import com.campusconnect.qa.entity.Tag;
import com.campusconnect.qa.repository.AnswerRepository;
import com.campusconnect.qa.repository.QuestionAnswerCount;
import com.campusconnect.qa.repository.QuestionRepository;
import com.campusconnect.qa.repository.VoteRepository;
import com.campusconnect.qa.service.AnswerVoteSummary;
import com.campusconnect.qa.service.VoteService;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.repository.UserRepository;
import com.campusconnect.user.service.UserService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;
  private final VoteRepository voteRepository;
  private final LostFoundPostRepository lostFoundPostRepository;
  private final ClaimRequestRepository claimRequestRepository;
  private final VoteService voteService;
  private final UserService userService;

  @Transactional(readOnly = true)
  public AdminDashboardResponse getDashboardStats() {
    return AdminDashboardResponse.builder()
        .totalUsers(userRepository.count())
        .activeUsers(userRepository.countByIsActiveTrue())
        .totalQuestions(questionRepository.count())
        .totalAnswers(answerRepository.count())
        .totalLostFoundPosts(lostFoundPostRepository.count())
        .openLostFoundPosts(lostFoundPostRepository.countByStatus(ItemStatus.OPEN))
        .totalClaims(claimRequestRepository.count())
        .pendingClaims(claimRequestRepository.countByStatus(ClaimStatus.PENDING))
        .build();
  }

  @Transactional(readOnly = true)
  public List<AdminUserResponse> getUsers() {
    return userRepository.findAllByOrderByIdDesc().stream()
        .map(this::toAdminUserResponse)
        .toList();
  }

  @Transactional
  public AdminUserResponse deactivateUser(Long userId) {
    User actor = userService.getCurrentUserEntity();
    User target = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (actor.getId().equals(target.getId())) {
      throw new BadRequestException("You cannot deactivate your own account");
    }

    target.setIsActive(false);
    User saved = userRepository.save(target);
    log.info("Admin deactivated user: admin={} target={}", actor.getEmail(), saved.getEmail());
    return toAdminUserResponse(saved);
  }

  @Transactional
  public AdminUserResponse activateUser(Long userId) {
    User actor = userService.getCurrentUserEntity();
    User target = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    target.setIsActive(true);
    User saved = userRepository.save(target);
    log.info("Admin activated user: admin={} target={}", actor.getEmail(), saved.getEmail());
    return toAdminUserResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<AdminQuestionResponse> getQuestions() {
    List<Question> questions = questionRepository.findAllForAdminOrderByIdDesc();
    Map<Long, Long> answerCounts = loadAnswerCounts(questions);

    return questions.stream()
        .map(question -> toAdminQuestionResponse(question, answerCounts.getOrDefault(question.getId(), 0L)))
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminQuestionDetailResponse getQuestionDetail(Long questionId) {
    Question question = questionRepository.findWithUserAndTagsById(questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

    List<AdminAnswerResponse> answers = getQuestionAnswers(questionId);

    return AdminQuestionDetailResponse.builder()
        .id(question.getId())
        .title(question.getTitle())
        .description(question.getDescription())
        .author(toUserSummary(question.getUser()))
        .tags(question.getTags().stream().map(Tag::getName).sorted().toList())
        .createdAt(question.getCreatedAt())
        .answers(answers)
        .build();
  }

  @Transactional(readOnly = true)
  public List<AdminAnswerResponse> getQuestionAnswers(Long questionId) {
    if (!questionRepository.existsById(questionId)) {
      throw new ResourceNotFoundException("Question not found");
    }

    List<Answer> answers = answerRepository.findByQuestionIdWithUserOrderByCreatedAtAsc(questionId);
    Map<Long, AnswerVoteSummary> voteSummaries = voteService.loadVoteSummaries(answers.stream().map(Answer::getId).toList());

    return answers.stream()
        .map(answer -> toAdminAnswerResponse(answer, voteSummaries.getOrDefault(answer.getId(), AnswerVoteSummary.zero())))
        .toList();
  }

  @Transactional
  public void deleteQuestion(Long questionId) {
    User actor = userService.getCurrentUserEntity();
    Question question = questionRepository.findWithUserAndTagsById(questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

    List<Long> answerIds = answerRepository.findIdsByQuestionId(questionId);
    if (!answerIds.isEmpty()) {
      voteRepository.deleteByAnswerIdIn(answerIds);
    }
    question.getTags().clear();
    questionRepository.save(question);
    answerRepository.deleteByQuestionId(questionId);
    questionRepository.delete(question);

    log.info("Admin deleted question: admin={} questionId={}", actor.getEmail(), questionId);
  }

  @Transactional
  public void deleteAnswer(Long answerId) {
    User actor = userService.getCurrentUserEntity();
    Answer answer = answerRepository.findByIdWithUserAndQuestion(answerId)
        .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

    voteRepository.deleteByAnswerId(answerId);
    answerRepository.delete(answer);

    log.info("Admin deleted answer: admin={} answerId={} questionId={}", actor.getEmail(), answerId,
        answer.getQuestion().getId());
  }

  @Transactional(readOnly = true)
  public List<AdminLostFoundResponse> getLostFoundPosts() {
    return lostFoundPostRepository.findAllByOrderByIdDesc().stream()
        .map(this::toAdminLostFoundResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminLostFoundResponse getLostFoundPost(Long postId) {
    LostFoundPost post = lostFoundPostRepository.findWithUserById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    return toAdminLostFoundResponse(post);
  }

  @Transactional
  public void deleteLostFoundPost(Long postId) {
    User actor = userService.getCurrentUserEntity();
    LostFoundPost post = lostFoundPostRepository.findWithUserById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    claimRequestRepository.deleteByPostId(postId);
    lostFoundPostRepository.delete(post);

    log.info("Admin deleted lost/found post: admin={} postId={}", actor.getEmail(), postId);
  }

  @Transactional(readOnly = true)
  public List<AdminClaimResponse> getClaims() {
    return claimRequestRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(this::toAdminClaimResponse)
        .toList();
  }

  private AdminUserResponse toAdminUserResponse(User user) {
    return AdminUserResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .course(user.getCourse())
        .yearOfStudy(user.getYearOfStudy())
        .reputationPoints(user.getReputationPoints())
        .isActive(user.getIsActive())
        .roles(user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()))
        .createdAt(user.getCreatedAt())
        .build();
  }

  private AdminQuestionResponse toAdminQuestionResponse(Question question, long answerCount) {
    return AdminQuestionResponse.builder()
        .id(question.getId())
        .title(question.getTitle())
        .author(toUserSummary(question.getUser()))
        .tags(question.getTags().stream().map(Tag::getName).sorted().toList())
        .answerCount(answerCount)
        .createdAt(question.getCreatedAt())
        .build();
  }

  private AdminAnswerResponse toAdminAnswerResponse(Answer answer, AnswerVoteSummary summary) {
    return AdminAnswerResponse.builder()
        .id(answer.getId())
        .content(answer.getContent())
        .author(toUserSummary(answer.getUser()))
        .createdAt(answer.getCreatedAt())
        .isAccepted(answer.getIsAccepted())
        .upvoteCount(summary.upvotes())
        .downvoteCount(summary.downvotes())
        .score(summary.score())
        .build();
  }

  private AdminLostFoundResponse toAdminLostFoundResponse(LostFoundPost post) {
    return AdminLostFoundResponse.builder()
        .id(post.getId())
        .type(post.getType())
        .title(post.getTitle())
        .description(post.getDescription())
        .imageUrl(post.getImageUrl())
        .owner(toUserSummary(post.getUser()))
        .location(post.getLocation())
        .dateOfIncident(post.getDateOfIncident())
        .status(post.getStatus())
        .createdAt(post.getCreatedAt())
        .build();
  }

  private AdminClaimResponse toAdminClaimResponse(ClaimRequest claim) {
    return AdminClaimResponse.builder()
        .id(claim.getId())
        .postId(claim.getPost().getId())
        .postTitle(claim.getPost().getTitle())
        .claimer(toUserSummary(claim.getClaimer()))
        .message(claim.getMessage())
        .status(claim.getStatus())
        .createdAt(claim.getCreatedAt())
        .build();
  }

  private UserSummaryResponse toUserSummary(User user) {
    return UserSummaryResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .build();
  }

  private Map<Long, Long> loadAnswerCounts(List<Question> questions) {
    if (questions == null || questions.isEmpty()) {
      return Collections.emptyMap();
    }

    List<QuestionAnswerCount> counts = answerRepository.countAnswersByQuestionIds(
        questions.stream().map(Question::getId).toList()
    );

    Map<Long, Long> map = new HashMap<>();
    for (QuestionAnswerCount count : counts) {
      if (count.getQuestionId() != null && count.getAnswerCount() != null) {
        map.put(count.getQuestionId(), count.getAnswerCount());
      }
    }
    return map;
  }
}
