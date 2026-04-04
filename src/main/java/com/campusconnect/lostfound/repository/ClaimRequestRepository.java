package com.campusconnect.lostfound.repository;

import com.campusconnect.enums.ClaimStatus;
import com.campusconnect.lostfound.entity.ClaimRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Long> {

  @EntityGraph(attributePaths = {"claimer", "post"})
  List<ClaimRequest> findByPostIdOrderByCreatedAtDesc(Long postId);

  @EntityGraph(attributePaths = {"claimer", "post", "post.user"})
  List<ClaimRequest> findAllByOrderByCreatedAtDesc();

  boolean existsByPostIdAndClaimerIdAndStatus(Long postId, Long claimerId, ClaimStatus status);

  long countByStatus(ClaimStatus status);

  @EntityGraph(attributePaths = {"post", "post.user", "claimer"})
  Optional<ClaimRequest> findWithPostAndUsersById(Long id);

  @EntityGraph(attributePaths = {"claimer", "post"})
  List<ClaimRequest> findByPostIdAndStatusOrderByCreatedAtDesc(Long postId, ClaimStatus status);

  void deleteByPostId(Long postId);
}
