package com.campusconnect.user.service;

import com.campusconnect.common.exception.UnauthorizedException;
import com.campusconnect.user.dto.UserResponse;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.repository.UserRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getCurrentUserEntity() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
      throw new UnauthorizedException("Unauthorized");
    }

    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
  }

  public UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .course(user.getCourse())
        .yearOfStudy(user.getYearOfStudy())
        .reputationPoints(user.getReputationPoints())
        .isActive(user.getIsActive())
        .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
