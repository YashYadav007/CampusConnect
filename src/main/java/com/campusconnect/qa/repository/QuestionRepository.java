package com.campusconnect.qa.repository;

import com.campusconnect.qa.entity.Question;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {
  @Query("""
      select distinct q from Question q
      join fetch q.user
      left join fetch q.tags
      order by q.id desc
      """)
  List<Question> findAllForAdminOrderByIdDesc();

  @EntityGraph(attributePaths = {"user"})
  @Query("""
      select q from Question q
      where lower(q.title) like lower(concat('%', :keyword, '%'))
      """)
  Page<Question> searchByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"user", "tags"})
  Optional<Question> findWithUserAndTagsById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select q from Question q where q.id = :id")
  Optional<Question> findByIdForUpdate(@Param("id") Long id);
}
