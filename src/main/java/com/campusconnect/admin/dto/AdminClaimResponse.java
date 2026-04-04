package com.campusconnect.admin.dto;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.enums.ClaimStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminClaimResponse {
  private Long id;
  private Long postId;
  private String postTitle;
  private UserSummaryResponse claimer;
  private String message;
  private ClaimStatus status;
  private LocalDateTime createdAt;
}
