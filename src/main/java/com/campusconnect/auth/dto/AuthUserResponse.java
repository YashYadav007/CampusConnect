package com.campusconnect.auth.dto;

import com.campusconnect.enums.RoleName;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserResponse {
  private Long id;
  private String fullName;
  private String email;
  private Set<RoleName> roles;
}

