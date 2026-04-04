package com.campusconnect.lostfound.service;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.common.exception.UnauthorizedException;
import com.campusconnect.enums.ClaimStatus;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
import com.campusconnect.lostfound.dto.ClaimResponseDto;
import com.campusconnect.lostfound.dto.CreateClaimRequestDto;
import com.campusconnect.lostfound.entity.ClaimRequest;
import com.campusconnect.lostfound.entity.LostFoundPost;
import com.campusconnect.lostfound.repository.ClaimRequestRepository;
import com.campusconnect.lostfound.repository.LostFoundPostRepository;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClaimService {

  private final ClaimRequestRepository claimRequestRepository;
  private final LostFoundPostRepository lostFoundPostRepository;
  private final UserService userService;

  @Transactional
  public ClaimResponseDto createClaim(Long postId, CreateClaimRequestDto req) {
    User currentUser = userService.getCurrentUserEntity();

    // Serialize claims per post to prevent duplicate PENDING claims under concurrency.
    LostFoundPost post = lostFoundPostRepository.findByIdForUpdate(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    validateClaimable(post, currentUser);

    String message = requireTrimmed(req.getMessage(), "message");
    if (message.length() < 10 || message.length() > 1000) {
      throw new BadRequestException("message must be between 10 and 1000 characters");
    }

    if (claimRequestRepository.existsByPostIdAndClaimerIdAndStatus(postId, currentUser.getId(), ClaimStatus.PENDING)) {
      throw new BadRequestException("You already have a pending claim on this post");
    }

    ClaimRequest claim = ClaimRequest.builder()
        .post(post)
        .claimer(currentUser)
        .message(message)
        .status(ClaimStatus.PENDING)
        .build();

    ClaimRequest saved = claimRequestRepository.save(claim);
    log.info("Claim created: id={} postId={} claimer={}", saved.getId(), postId, currentUser.getEmail());

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ClaimResponseDto> getClaimsForPost(Long postId) {
    User currentUser = userService.getCurrentUserEntity();

    LostFoundPost post = lostFoundPostRepository.findWithUserById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (post.getType() != PostType.FOUND) {
      throw new BadRequestException("Claims are only available for FOUND posts");
    }

    if (!post.getUser().getId().equals(currentUser.getId())) {
      throw new UnauthorizedException("Only the post owner can view claims");
    }

    return claimRequestRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public ClaimResponseDto approveClaim(Long claimId) {
    User currentUser = userService.getCurrentUserEntity();

    ClaimRequest claim = claimRequestRepository.findWithPostAndUsersById(claimId)
        .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

    LostFoundPost post = claim.getPost();
    if (!post.getUser().getId().equals(currentUser.getId())) {
      throw new UnauthorizedException("Only the post owner can approve claims");
    }

    if (claim.getStatus() != ClaimStatus.PENDING) {
      throw new BadRequestException("Only pending claims can be approved");
    }

    // Serialize approval per post for consistency (post status, other claims).
    LostFoundPost lockedPost = lostFoundPostRepository.findByIdForUpdate(post.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (!lockedPost.getId().equals(claim.getPost().getId())) {
      // Defensive check in case future refactors change fetch shapes.
      throw new BadRequestException("Claim does not belong to this post");
    }

    if (lockedPost.getType() != PostType.FOUND) {
      throw new BadRequestException("Only FOUND posts can have claims approved");
    }
    if (lockedPost.getStatus() != ItemStatus.OPEN) {
      throw new BadRequestException("Post is not open for claiming");
    }

    // Reject all other pending claims for this post.
    List<ClaimRequest> pendings = claimRequestRepository.findByPostIdAndStatusOrderByCreatedAtDesc(
        lockedPost.getId(),
        ClaimStatus.PENDING
    );

    for (ClaimRequest c : pendings) {
      if (c.getId().equals(claim.getId())) {
        c.setStatus(ClaimStatus.APPROVED);
      } else {
        c.setStatus(ClaimStatus.REJECTED);
      }
    }

    lockedPost.setStatus(ItemStatus.RESOLVED);
    lostFoundPostRepository.save(lockedPost);
    claimRequestRepository.saveAll(pendings);

    ClaimRequest saved = pendings.stream().filter(c -> c.getId().equals(claim.getId())).findFirst().orElse(claim);

    log.info("Claim approved: claimId={} postId={} byOwner={}", saved.getId(), lockedPost.getId(), currentUser.getEmail());
    return toResponse(saved);
  }

  @Transactional
  public ClaimResponseDto rejectClaim(Long claimId) {
    User currentUser = userService.getCurrentUserEntity();

    ClaimRequest claim = claimRequestRepository.findWithPostAndUsersById(claimId)
        .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

    if (!claim.getPost().getUser().getId().equals(currentUser.getId())) {
      throw new UnauthorizedException("Only the post owner can reject claims");
    }

    if (claim.getStatus() != ClaimStatus.PENDING) {
      throw new BadRequestException("Only pending claims can be rejected");
    }

    claim.setStatus(ClaimStatus.REJECTED);
    ClaimRequest saved = claimRequestRepository.save(claim);
    log.info("Claim rejected: claimId={} postId={} byOwner={}", saved.getId(), saved.getPost().getId(), currentUser.getEmail());
    return toResponse(saved);
  }

  private void validateClaimable(LostFoundPost post, User currentUser) {
    if (post.getType() != PostType.FOUND) {
      throw new BadRequestException("Only FOUND posts can be claimed");
    }
    if (post.getStatus() != ItemStatus.OPEN) {
      throw new BadRequestException("Post is not open for claiming");
    }
    if (post.getUser().getId().equals(currentUser.getId())) {
      throw new BadRequestException("You cannot claim your own post");
    }
  }

  private ClaimResponseDto toResponse(ClaimRequest c) {
    return ClaimResponseDto.builder()
        .id(c.getId())
        .postId(c.getPost().getId())
        .postTitle(c.getPost().getTitle())
        .postStatus(c.getPost().getStatus())
        .claimer(UserSummaryResponse.builder()
            .id(c.getClaimer().getId())
            .fullName(c.getClaimer().getFullName())
            .build())
        .message(c.getMessage())
        .status(c.getStatus())
        .createdAt(c.getCreatedAt())
        .build();
  }

  private String requireTrimmed(String raw, String field) {
    if (raw == null) {
      throw new BadRequestException(field + " is required");
    }
    String t = raw.trim();
    if (t.isBlank()) {
      throw new BadRequestException(field + " is required");
    }
    return t;
  }
}
