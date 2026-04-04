package com.campusconnect.admin.dto;

import com.campusconnect.enums.RoleName;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
  private Long id;
  private String fullName;
  private String email;
  private String course;
  private Integer yearOfStudy;
  private Integer reputationPoints;
  private Boolean isActive;
  private Set<RoleName> roles;
  private LocalDateTime createdAt;
}
