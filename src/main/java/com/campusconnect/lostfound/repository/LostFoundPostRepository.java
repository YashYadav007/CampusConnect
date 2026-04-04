package com.campusconnect.lostfound.repository;

import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
import com.campusconnect.lostfound.entity.LostFoundPost;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LostFoundPostRepository extends JpaRepository<LostFoundPost, Long> {

  @EntityGraph(attributePaths = {"user"})
  java.util.List<LostFoundPost> findAllByOrderByIdDesc();

  @EntityGraph(attributePaths = {"user"})
  Page<LostFoundPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<LostFoundPost> findByTypeOrderByCreatedAtDesc(PostType type, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<LostFoundPost> findByStatusOrderByCreatedAtDesc(ItemStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<LostFoundPost> findByTypeAndStatusOrderByCreatedAtDesc(PostType type, ItemStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Optional<LostFoundPost> findWithUserById(Long id);

  long countByStatus(ItemStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from LostFoundPost p where p.id = :id")
  Optional<LostFoundPost> findByIdForUpdate(@Param("id") Long id);
}
