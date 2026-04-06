package com.campusconnect.lostfound.service;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
import com.campusconnect.lostfound.dto.CreateLostFoundPostRequest;
import com.campusconnect.lostfound.dto.LostFoundPostResponse;
import com.campusconnect.lostfound.entity.LostFoundPost;
import com.campusconnect.lostfound.repository.LostFoundPostRepository;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LostFoundService {

  private static final int MAX_PAGE_SIZE = 50;

  private final LostFoundPostRepository lostFoundPostRepository;
  private final UserService userService;

  @Transactional
  public LostFoundPostResponse createPost(CreateLostFoundPostRequest req) {
    User currentUser = userService.getCurrentUserEntity();

    String title = requireTrimmed(req.getTitle(), "title");
    String location = requireTrimmed(req.getLocation(), "location");

    String description = normalizeOptionalText(req.getDescription());
    String imageUrl = normalizeOptionalText(req.getImageUrl());

    if (req.getType() == null) {
      throw new BadRequestException("type is required");
    }
    if (req.getDateOfIncident() == null) {
      throw new BadRequestException("dateOfIncident is required");
    }

    LostFoundPost post = LostFoundPost.builder()
        .user(currentUser)
        .type(req.getType())
        .title(title)
        .description(description)
        .imageUrl(imageUrl)
        .location(location)
        .dateOfIncident(req.getDateOfIncident())
        .status(ItemStatus.OPEN)
        .build();

    LostFoundPost saved = lostFoundPostRepository.save(post);
    log.info("LostFound post created: id={} user={} type={}", saved.getId(), currentUser.getEmail(), saved.getType());

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<LostFoundPostResponse> getAllPosts(int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size));
    Page<LostFoundPost> posts = lostFoundPostRepository.findAllByOrderByCreatedAtDesc(pageable);
    return posts.getContent().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public LostFoundPostResponse getPostById(Long id) {
    LostFoundPost post = lostFoundPostRepository.findWithUserById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    return toResponse(post);
  }

  @Transactional(readOnly = true)
  public List<LostFoundPostResponse> filterPosts(PostType type, ItemStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size));

    Page<LostFoundPost> posts;
    if (type != null && status != null) {
      posts = lostFoundPostRepository.findByTypeAndStatusOrderByCreatedAtDesc(type, status, pageable);
    } else if (type != null) {
      posts = lostFoundPostRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    } else if (status != null) {
      posts = lostFoundPostRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    } else {
      posts = lostFoundPostRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    return posts.getContent().stream().map(this::toResponse).toList();
  }

  private LostFoundPostResponse toResponse(LostFoundPost p) {
    return LostFoundPostResponse.builder()
        .id(p.getId())
        .type(p.getType())
        .title(p.getTitle())
        .description(p.getDescription())
        .imageUrl(p.getImageUrl())
        .location(p.getLocation())
        .dateOfIncident(p.getDateOfIncident())
        .status(p.getStatus())
        .createdAt(p.getCreatedAt())
        .user(UserSummaryResponse.builder().id(p.getUser().getId()).fullName(p.getUser().getFullName()).build())
        .ownerEmail(p.getUser().getEmail())
        .build();
  }

  private int clampPageSize(int size) {
    if (size <= 0) {
      return 20;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  private String normalizeOptionalText(String raw) {
    if (raw == null) {
      return null;
    }
    String t = raw.trim();
    return t.isBlank() ? null : t;
  }

  private String requireTrimmed(String raw, String field) {
    String t = normalizeOptionalText(raw);
    if (t == null) {
      throw new BadRequestException(field + " is required");
    }
    return t;
  }
}
