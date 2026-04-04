package com.campusconnect.qa.repository;

import com.campusconnect.qa.entity.Answer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
  List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

  @EntityGraph(attributePaths = {"user"})
  List<Answer> findByQuestionIdOrderByIdDesc(Long questionId);

  @Query("select a.id from Answer a where a.question.id = :questionId")
  List<Long> findIdsByQuestionId(@Param("questionId") Long questionId);

  long countByQuestionId(Long questionId);

  void deleteByQuestionId(Long questionId);

  @Query("""
      select a from Answer a
      join fetch a.user au
      join fetch a.question q
      join fetch q.user qu
      where a.id = :answerId
      """)
  Optional<Answer> findByIdWithQuestionAndUsers(@Param("answerId") Long answerId);

  @Query("""
      select a from Answer a
      join fetch a.user au
      join fetch a.question q
      where a.id = :answerId
      """)
  Optional<Answer> findByIdWithUserAndQuestion(@Param("answerId") Long answerId);

  @Query("""
      select a from Answer a
      join fetch a.user au
      where a.question.id = :questionId
        and a.isAccepted = true
      """)
  Optional<Answer> findAcceptedByQuestionId(@Param("questionId") Long questionId);

  @Query("""
      select a from Answer a
      join fetch a.user u
      where a.question.id = :questionId
      order by a.createdAt asc
      """)
  List<Answer> findByQuestionIdWithUserOrderByCreatedAtAsc(@Param("questionId") Long questionId);

  @Query("""
      select a.question.id as questionId, count(a) as answerCount
      from Answer a
      where a.question.id in :questionIds
      group by a.question.id
      """)
  List<QuestionAnswerCount> countAnswersByQuestionIds(@Param("questionIds") List<Long> questionIds);
}
