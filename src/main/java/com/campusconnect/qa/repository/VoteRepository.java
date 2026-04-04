package com.campusconnect.qa.repository;

import com.campusconnect.enums.VoteType;
import com.campusconnect.qa.entity.Vote;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

public interface VoteRepository extends JpaRepository<Vote, Long> {

  Optional<Vote> findByAnswerIdAndUserId(Long answerId, Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Vote> findForUpdateByAnswerIdAndUserId(Long answerId, Long userId);

  boolean existsByAnswerIdAndUserId(Long answerId, Long userId);

  long countByAnswerIdAndVoteType(Long answerId, VoteType voteType);

  void deleteByAnswerId(Long answerId);

  void deleteByAnswerIdIn(List<Long> answerIds);

  @Query("""
      select v.answer.id as answerId, v.voteType as voteType, count(v) as cnt
      from Vote v
      where v.answer.id in :answerIds
      group by v.answer.id, v.voteType
      """)
  List<AnswerVoteCount> countVotesByAnswerIds(@Param("answerIds") List<Long> answerIds);
}
