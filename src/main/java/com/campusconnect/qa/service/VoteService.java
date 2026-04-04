package com.campusconnect.qa.service;

import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.enums.VoteType;
import com.campusconnect.qa.dto.AnswerResponse;
import com.campusconnect.qa.dto.AuthorResponse;
import com.campusconnect.qa.entity.Answer;
import com.campusconnect.qa.entity.Vote;
import com.campusconnect.qa.repository.AnswerRepository;
import com.campusconnect.qa.repository.AnswerVoteCount;
import com.campusconnect.qa.repository.VoteRepository;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoteService {

  private static final int REP_UPVOTE = 10;
  private static final int REP_DOWNVOTE = -2;

  private final VoteRepository voteRepository;
  private final AnswerRepository answerRepository;
  private final UserService userService;

  @Transactional
  public AnswerResponse vote(Long answerId, VoteType voteType) {
    User voter = userService.getCurrentUserEntity();

    Answer answer = answerRepository.findByIdWithUserAndQuestion(answerId)
        .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

    if (answer.getUser().getId().equals(voter.getId())) {
      throw new BadRequestException("You cannot vote on your own answer");
    }

    // Toggle/update behaviour
    // Lock existing vote row (if any) to avoid concurrent toggle/update double-applying reputation.
    Vote existing = voteRepository.findForUpdateByAnswerIdAndUserId(answerId, voter.getId()).orElse(null);

    if (existing == null) {
      createVoteWithRaceHandling(answer, voter, voteType);
      applyReputationDelta(answer.getUser(), deltaFor(voteType));
      log.info("Vote created: answerId={} voterId={} type={}", answerId, voter.getId(), voteType);
    } else if (existing.getVoteType() == voteType) {
      // Toggle off
      voteRepository.delete(existing);
      applyReputationDelta(answer.getUser(), -deltaFor(existing.getVoteType()));
      log.info("Vote removed: answerId={} voterId={} type={}", answerId, voter.getId(), voteType);
    } else {
      // Change vote type
      VoteType old = existing.getVoteType();
      existing.setVoteType(voteType);
      voteRepository.save(existing);

      // Reverse old, apply new
      applyReputationDelta(answer.getUser(), -deltaFor(old));
      applyReputationDelta(answer.getUser(), deltaFor(voteType));

      log.info("Vote updated: answerId={} voterId={} {}->{}", answerId, voter.getId(), old, voteType);
    }

    return buildAnswerResponseWithVotes(answer);
  }

  private void createVoteWithRaceHandling(Answer answer, User voter, VoteType voteType) {
    try {
      voteRepository.save(Vote.builder().answer(answer).user(voter).voteType(voteType).build());
    } catch (DataIntegrityViolationException ex) {
      // If a concurrent request created the vote, treat it as "already voted".
      Vote existing = voteRepository.findByAnswerIdAndUserId(answer.getId(), voter.getId()).orElse(null);
      if (existing != null) {
        throw new BadRequestException("You have already cast this vote");
      }
      throw ex;
    }
  }

  private int deltaFor(VoteType voteType) {
    return voteType == VoteType.UPVOTE ? REP_UPVOTE : REP_DOWNVOTE;
  }

  private void applyReputationDelta(User answerAuthor, int delta) {
    answerAuthor.setReputationPoints(answerAuthor.getReputationPoints() + delta);
  }

  private AnswerResponse buildAnswerResponseWithVotes(Answer answer) {
    Map<Long, AnswerVoteSummary> summaries = loadVoteSummaries(List.of(answer.getId()));
    AnswerVoteSummary s = summaries.getOrDefault(answer.getId(), AnswerVoteSummary.zero());

    return AnswerResponse.builder()
        .id(answer.getId())
        .content(answer.getContent())
        .user(AuthorResponse.builder().id(answer.getUser().getId()).fullName(answer.getUser().getFullName()).build())
        .createdAt(answer.getCreatedAt())
        .isAccepted(answer.getIsAccepted())
        .upvoteCount(s.upvotes())
        .downvoteCount(s.downvotes())
        .score(s.score())
        .build();
  }

  public Map<Long, AnswerVoteSummary> loadVoteSummaries(List<Long> answerIds) {
    if (answerIds == null || answerIds.isEmpty()) {
      return Map.of();
    }

    List<AnswerVoteCount> rows = voteRepository.countVotesByAnswerIds(answerIds);

    Map<Long, AnswerVoteSummary> map = new HashMap<>();
    for (Long id : answerIds) {
      map.put(id, AnswerVoteSummary.zero());
    }

    for (AnswerVoteCount r : rows) {
      Long id = r.getAnswerId();
      if (id == null) {
        continue;
      }
      AnswerVoteSummary current = map.getOrDefault(id, AnswerVoteSummary.zero());
      long cnt = r.getCnt() == null ? 0L : r.getCnt();
      if (r.getVoteType() == VoteType.UPVOTE) {
        map.put(id, new AnswerVoteSummary(cnt, current.downvotes()));
      } else {
        map.put(id, new AnswerVoteSummary(current.upvotes(), cnt));
      }
    }

    return map;
  }
}
